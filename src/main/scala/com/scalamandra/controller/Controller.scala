package com.scalamandra.controller

import com.scalamandra.model.HttpException
import com.scalamandra.serialization.uPickleTapir
import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.tapir.{Codec, CodecFormat, EndpointOutput}
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

trait Controller extends uPickleTapir {

  type Endpoint = ServerEndpoint[AkkaStreams with WebSockets, Future]

  def endpoints: List[Endpoint]

  protected final def oneOfHttp[T <: HttpException](value: T)
                                                   (implicit ev: Codec[String, T, CodecFormat.TextPlain]): EndpointOutput.OneOfVariant[T] = {
    import sttp.tapir._
    oneOfVariant(
      statusCode(value.statusCode)
        .and(
          plainBody[T].description(value.getMessage)
        )
    )
  }

}