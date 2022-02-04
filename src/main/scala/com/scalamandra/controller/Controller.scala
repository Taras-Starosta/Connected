package com.scalamandra.controller

import com.scalamandra.config.ApiConfig
import com.scalamandra.model.HttpException
import com.scalamandra.serialization.uPickleTapir
import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.tapir.server.ServerEndpoint
import sttp.tapir._
import sttp.tapir.typelevel.ErasureSameAsType

import scala.concurrent.Future
import scala.reflect.ClassTag

trait Controller extends uPickleTapir {

  def apiConfig: ApiConfig

  lazy val version: EndpointInput[Unit] = "api" / s"v${apiConfig.version}"

  type Endpoint = ServerEndpoint[AkkaStreams with WebSockets, Future]

  def endpoints: List[Endpoint]

  protected final def oneOfHttp[T <: HttpException: ClassTag: ErasureSameAsType](value: T)
                                                                                (implicit ev: Codec[String, T, CodecFormat.TextPlain]): EndpointOutput.OneOfVariant[T] =
    HttpException.oneOf(value)

}