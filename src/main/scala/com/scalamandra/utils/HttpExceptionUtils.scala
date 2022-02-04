package com.scalamandra.utils

import akka.http.scaladsl.util.FastFuture
import com.scalamandra.model.HttpException
import sttp.tapir.{Codec, CodecFormat, EndpointOutput}
import sttp.tapir.typelevel.ErasureSameAsType

import scala.concurrent.Future
import scala.reflect.ClassTag

object HttpExceptionUtils {

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

}