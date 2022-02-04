package com.scalamandra.model

import akka.http.scaladsl.util.FastFuture
import sttp.model.StatusCode
import sttp.tapir.Codec.PlainCodec
import sttp.tapir.typelevel.ErasureSameAsType
import sttp.tapir.{Codec, CodecFormat, DecodeResult, EndpointOutput}

import scala.concurrent.Future
import scala.reflect.ClassTag

sealed abstract class HttpException(
                                     val statusCode: StatusCode,
                                     message: String,
                                     reason: Throwable = null.asInstanceOf[Throwable],
                                   ) extends Exception(message, reason)
object HttpException {

  final def oneOf[T <: HttpException: ClassTag: ErasureSameAsType](value: T)
                                                                  (implicit ev: Codec[String, T, CodecFormat.TextPlain]): EndpointOutput.OneOfVariant[T] = {
    import sttp.tapir._
    oneOfVariant(
      statusCode(value.statusCode)
        .and(
          plainBody[T].description(value.getMessage)
        )
    )
  }

  final def error[T <: HttpException](obj: T): Future[Left[T, Nothing]] = FastFuture.successful(Left(obj))

  sealed abstract class Unauthorized(
                                      message: String,
                                      reason: Throwable = null.asInstanceOf[Throwable],
                                    ) extends HttpException(StatusCode.Unauthorized, message, reason)

  case object InvalidJwt extends Unauthorized("Invalid jwt.")

  case object InvalidCredentials extends Unauthorized("invalid credentials.")

  sealed abstract class NotFound(what: String) extends HttpException(StatusCode.NotFound, s"$what not found.")

  case object UserNotFound extends NotFound("User")

  case object ConfirmationNotFound extends NotFound("Pending confirmation")

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