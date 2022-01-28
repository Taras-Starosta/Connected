package com.scalamandra

import akka.actor.typed.{ActorSystem, SpawnProtocol}
import akka.http.scaladsl.util.FastFuture
import com.scalamandra.config._
import com.scalamandra.logging.LoggerConfigurator
import com.scalamandra.server.impl.ServerImpl
import pureconfig._
import pureconfig.error.ConfigReaderException

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.control.NonFatal

object Main {

  private lazy val source: ConfigObjectSource = ConfigSource.default

  private def loadConfig[T: ConfigReader: ClassTag](path: String): Future[T] =
    source.at(path).load match {
      case Left(failures) =>
        FastFuture.failed(new ConfigReaderException[T](failures))
      case Right(value) =>
        FastFuture.successful(value)
    }

  def main(args: Array[String]): Unit = {
    implicit val actorSystem: ActorSystem[SpawnProtocol.Command] = ActorSystem(SpawnProtocol(), "Connected")
    LoggerConfigurator()
    implicit val ec: ExecutionContext = actorSystem.executionContext
    val helloWorld = new HelloWorldController
    val bootstrap = for {
      serverConfig <- loadConfig[ServerConfig]("server")
      apiConfig <- loadConfig[ApiConfig]("api")
      _ <- new ServerImpl(serverConfig, apiConfig, List(helloWorld)).start()
    } yield ()
    bootstrap.recover {
      case NonFatal(exc) =>
        scribe.error(exc)
        actorSystem.terminate()
    }
  }

}