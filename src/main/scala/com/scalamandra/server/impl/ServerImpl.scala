package com.scalamandra.server.impl

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.util.FastFuture
import com.scalamandra.config.{ApiConfig, ServerConfig}
import com.scalamandra.controller.Controller
import com.scalamandra.logging.ServerLogger
import com.scalamandra.server.Server
import com.scalamandra.utils.Blocker
import sttp.tapir.model.ServerRequest
import sttp.tapir.server.akkahttp.{AkkaHttpServerInterpreter, AkkaHttpServerOptions}
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import java.io.File
import java.nio.file.Files
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class ServerImpl(
                  serverConfig: ServerConfig,
                  apiConfig: ApiConfig,
                  controllers: List[Controller],
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

  override def start(): Future[Unit] = {
    val endpoints = controllers.flatMap(_.endpoints)
    val docs = if(apiConfig.swagger) {
      SwaggerInterpreter().fromServerEndpoints[Future](
        endpoints,
        apiConfig.name,
        apiConfig.version,
      )
    } else List.empty

    val routes = AkkaHttpServerInterpreter(
      serverOptions
    ).toRoute(endpoints ++ docs)

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