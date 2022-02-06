package com.scalamandra.utils

import akka.http.scaladsl.util.FastFuture
import com.scalamandra.model.{HttpException, WsException}
import sttp.tapir.Codec.PlainCodec
import sttp.tapir.EndpointOutput.OneOfVariant
import sttp.tapir._
import sttp.tapir.typelevel.ErasureSameAsType

import scala.concurrent.Future
import scala.reflect.ClassTag

object ExceptionUtils {

  final def oneOfHttp[T <: HttpException: ClassTag: ErasureSameAsType: PlainCodec](value: T): OneOfVariant[T] =
    oneOfVariant(
      statusCode(value.statusCode)
        .and(
          plainBody[T].description(value.getMessage)
        )
    )

  final def oneOfWs[T <: WsException: ClassTag: ErasureSameAsType: PlainCodec](value: T): OneOfVariant[T] =
    oneOfVariant(plainBody[T].description(value.getMessage))

  final def error[T <: Exception](obj: T): Future[Left[T, Nothing]] = FastFuture.successful(Left(obj))

}