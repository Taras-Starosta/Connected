package com.scalamandra.controller

import com.scalamandra.config.ApiConfig
import com.scalamandra.model.{HttpException, WsException}
import com.scalamandra.serialization.uPickleTapir
import com.scalamandra.utils.ExceptionUtils
import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.tapir.Codec.PlainCodec
import sttp.tapir.EndpointOutput.OneOfVariant
import sttp.tapir._
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.typelevel.ErasureSameAsType

import scala.concurrent.Future
import scala.reflect.ClassTag

trait Controller extends uPickleTapir {

  def apiConfig: ApiConfig

  lazy val version: EndpointInput[Unit] = "api" / s"v${apiConfig.version}"

  type Endpoint = ServerEndpoint[AkkaStreams with WebSockets, Future]

  def endpoints: List[Endpoint]

  protected final def oneOfHttp[T <: HttpException: ClassTag: ErasureSameAsType: PlainCodec](value: T): OneOfVariant[T] =
    ExceptionUtils.oneOfHttp(value)

  protected final def oneOfWs[T <: WsException: ClassTag: ErasureSameAsType: PlainCodec](value: T): OneOfVariant[T] =
    ExceptionUtils.oneOfWs(value)

}