package com.scalamandra.service

import akka.http.scaladsl.util.FastFuture
import com.scalamandra.model.HttpException
import com.scalamandra.utils.HttpExceptionUtils

import scala.concurrent.Future

trait Service {

  protected final def error[T <: HttpException](obj: => T): Future[Left[T, Nothing]] = HttpExceptionUtils.error(obj)

  protected final def success[T](obj: => T): Future[Right[Nothing, T]] = FastFuture.successful(Right(obj))

}