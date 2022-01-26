package com.scalamandra.server.impl

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.util.FastFuture
import com.scalamandra.config.{ApiConfig, ServerConfig}
import com.scalamandra.controller.Controller
import com.scalamandra.server.Server
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class ServerImpl(
                  serverConfig: ServerConfig,
                  apiConfig: ApiConfig,
                  controllers: List[Controller],
                )(implicit
                  actorSystem: ActorSystem,
                  ec: ExecutionContext) extends Server {
  import serverConfig._

  override def start(): Future[Unit] = {
    val endpoints = controllers.flatMap(_.endpoints)
    val docs = if(apiConfig.swagger) {
      SwaggerInterpreter().fromServerEndpoints[Future](
        endpoints,
        apiConfig.name,
        apiConfig.version,
      )
    } else List.empty
    val routes = AkkaHttpServerInterpreter().toRoute(endpoints ++ docs)
    Http().newServerAt(host, port)
      .bind(routes)
      .transformWith {
        case Success(binding) =>
          scribe.info(s"Server started on $host:$port.")
          sys.addShutdownHook {
            binding.unbind()
          }
          Future.unit
        case Failure(exc) =>
          scribe.error(s"Server cannot start.", exc)
          FastFuture.failed(exc)
      }
  }

}