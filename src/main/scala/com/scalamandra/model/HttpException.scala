package com.scalamandra.model

import sttp.model.StatusCode
import sttp.tapir.Codec.PlainCodec
import sttp.tapir.{Codec, DecodeResult}

sealed abstract class HttpException(
                                     val statusCode: StatusCode,
                                     message: String,
                                     reason: Throwable = null.asInstanceOf[Throwable],
                                   ) extends Exception(message, reason)
object HttpException {

  sealed abstract class Unauthorized(
                                      message: String,
                                      reason: Throwable = null.asInstanceOf[Throwable],
                                    ) extends HttpException(StatusCode.Unauthorized, message, reason)

  case object InvalidJwt extends Unauthorized("Invalid jwt.")

  private val codec: PlainCodec[HttpException] = Codec.string.mapDecode(cantBeDeserialized)(_.getMessage)

  implicit def httpExceptionCodec[T <: HttpException]: PlainCodec[T] =
    codec.asInstanceOf[PlainCodec[T]]

  case object CantBeDeserialized extends Exception("Cant be deserialized.")

  def cantBeDeserialized(serialized: String): DecodeResult.Error =
    DecodeResult.Error(
      serialized,
      CantBeDeserialized,
    )

  sealed abstract class Conflict(reason: String) extends HttpException(StatusCode.Conflict, reason)

  case object UserAlreadyExists extends Conflict("User already exists.")

}