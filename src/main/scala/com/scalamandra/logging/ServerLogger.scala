package com.scalamandra.logging

import akka.actor.typed.ActorSystem
import com.scalamandra.utils.Blocker
import sttp.model.Headers
import sttp.tapir.AnyEndpoint
import sttp.tapir.model.{ServerRequest, ServerResponse}
import sttp.tapir.server.interceptor.log.ServerLog
import sttp.tapir.server.interceptor.{DecodeFailureContext, DecodeSuccessContext, SecurityFailureContext}

import scala.concurrent.Future

class ServerLogger(implicit val actorSystem: ActorSystem[_])
  extends ServerLog[Future] with Blocker {

  private def showResponse(response: ServerResponse[_]): String = {
    s"Response:\n\tStatus code: ${response.statusText}\n\tHeaders: ${Headers.toStringSafe(response.headers)}\n\tBody: ${response.body.getOrElse("<empty body>")}"
  }

  private def showDecodeFailure(ctx: DecodeFailureContext) =
    s"Decoding failure.\n${ctx.endpoint.showDetail}\nRequest: ${ctx.request}.\nInput: ${ctx.failingInput.show}.\nError: ${ctx.failure}."

  private def showDetails(endpoint: AnyEndpoint, request: ServerRequest, response: ServerResponse[_]): String =
    s"${endpoint.showDetail}\nRequest:$request\n${showResponse(response)}"

  override def decodeFailureNotHandled(ctx: DecodeFailureContext): Future[Unit] =
    blocking {
      scribe.error(
        showDecodeFailure(ctx)
      )
    }

  override def decodeFailureHandled(ctx: DecodeFailureContext,
                                    response: ServerResponse[_]): Future[Unit] =
    blocking {
      scribe.warn(
        s"${showDecodeFailure(ctx)}\n${showResponse(response)}"
      )
    }

  override def securityFailureHandled(ctx: SecurityFailureContext[Future, _],
                                      response: ServerResponse[_]): Future[Unit] =
    blocking {
      scribe.warn(
        s"Unauthorized request.\n${showDetails(ctx.endpoint, ctx.request, response)}"
      )
    }

  override def requestHandled(ctx: DecodeSuccessContext[Future, _, _],
                              response: ServerResponse[_]): Future[Unit] =
    blocking {
      scribe.debug(s"Handled request.\n${showDetails(ctx.endpoint, ctx.request, response)}")
    }

  override def exception(e: AnyEndpoint, request: ServerRequest, exc: Throwable): Future[Unit] =
    blocking {
      scribe.error(s"Exception during request processing.\n${e.showDetail}\nRequest:$request", exc)
    }

}