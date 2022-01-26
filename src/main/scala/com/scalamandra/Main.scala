package com.scalamandra

import akka.actor.ActorSystem
import akka.http.scaladsl.util.FastFuture
import com.scalamandra.config._
import com.scalamandra.controller.HelloWorldController
import com.scalamandra.server.impl.ServerImpl
import pureconfig._
import pureconfig.error.ConfigReaderException

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

object Main {

  private lazy val source: ConfigObjectSource = ConfigSource.default

  def loadConfig[T: ConfigReader: ClassTag](path: String): Future[T] =
    source.at(path).load match {
      case Left(failures) =>
        FastFuture.failed(new ConfigReaderException[T](failures))
      case Right(value) =>
        FastFuture.successful(value)
    }

  def main(args: Array[String]): Unit = {
    implicit val actorSystem: ActorSystem = ActorSystem("Connected")
    implicit val ec: ExecutionContext = ExecutionContext.global
    val helloWorld = new HelloWorldController
    for {
      serverConfig <- loadConfig[ServerConfig]("server")
      apiConfig <- loadConfig[ApiConfig]("api")
      _ <- new ServerImpl(serverConfig, apiConfig, List(helloWorld)).start()
    } yield ()
  }

}