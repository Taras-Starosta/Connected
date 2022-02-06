package com.scalamandra.server.impl

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import com.scalamandra.config.{ApiConfig, ServerConfig}
import com.scalamandra.controller.Controller
import com.scalamandra.logging.ServerLogger
import com.scalamandra.server.Server
import com.scalamandra.utils.Blocker
import sttp.tapir.docs.asyncapi.AsyncAPIInterpreter
import sttp.tapir.model.ServerRequest
import sttp.tapir.server.akkahttp.{AkkaHttpServerInterpreter, AkkaHttpServerOptions}
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import java.io.File
import java.nio.file.{Files, Path}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class ServerImpl(
                  serverConfig: ServerConfig,
                  apiConfig: ApiConfig,
                  controllers: List[Controller],
                  swaggerDocs: List[Controller],
                  asyncApiDocs: List[Controller],
                )(implicit
                  val actorSystem: ActorSystem[_],
                  ec: ExecutionContext) extends Server with Blocker {
  import serverConfig._

  def createTempFile(request: ServerRequest): Future[File] =
    blocking {
      Files.createTempFile("tapir", "tmp").toFile
    }

  def deleteFile(file: File): Future[Unit] =
    blocking {
      Files.delete(file.toPath)
    }

  def serverOptions: AkkaHttpServerOptions = AkkaHttpServerOptions(
    createFile = createTempFile,
    deleteFile = deleteFile,
    interceptors = List(
      AkkaHttpServerOptions.Log.serverLogInterceptor(new ServerLogger())
    )
  )

  override def start(): Future[ServerBinding] = {
    val endpoints = controllers.flatMap(_.endpoints)

    val swagger = if(apiConfig.swagger) {
      SwaggerInterpreter().fromServerEndpoints[Future](
        swaggerDocs.flatMap(_.endpoints),
        apiConfig.name,
        apiConfig.version,
      )
    } else List.empty

    if(apiConfig.asyncApiDocs) {
      import sttp.tapir.asyncapi._
      import sttp.tapir.asyncapi.circe.yaml._
      val docs = AsyncAPIInterpreter().serverEndpointsToAsyncAPI(
        asyncApiDocs.flatMap(_.endpoints),
        apiConfig.name,
        apiConfig.version,
        Vector("configured" -> Server(
          s"$host:$port",
          "ws",
        ))
      ).toYaml
      val source = Source.single(docs)
      val sink = FileIO.toPath(Path.of("asyncapi.yaml"))
      source.map(ByteString.apply)
        .runWith(sink)
        .onComplete {
          case Success(_) =>
            scribe.info("Async API file created.")
          case Failure(exc) =>
            scribe.error("Async API file cannot be created.", exc)
        }
    }

    val routes = AkkaHttpServerInterpreter(
      serverOptions
    ).toRoute(endpoints ++ swagger)

    Http().newServerAt(host, port).bind(routes)
  }

}