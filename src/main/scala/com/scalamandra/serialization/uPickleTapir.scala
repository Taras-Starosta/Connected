package com.scalamandra.serialization
import sttp.tapir.Codec.JsonCodec
import sttp.tapir.DecodeResult.Error.JsonDecodeException
import sttp.tapir.DecodeResult.{Error, Value}
import sttp.tapir._

import scala.util.{Failure, Success, Try}

trait uPickleTapir {

  def jsonBody[T: ReadWriter: Schema]: EndpointIO.Body[String, T] = anyFromUtf8StringBody(readWriterCodec[T])

  implicit def readWriterCodec[T: ReadWriter: Schema]: JsonCodec[T] =
    Codec.json[T] { s =>
      Try(read[T](s)) match {
        case Success(v) => Value(v)
        case Failure(e) => Error(s, JsonDecodeException(errors = List.empty, e))
      }
    } { t => write(t) }
}