package com.scalamandra.service

import com.scalamandra.model.HttpException
import com.scalamandra.utils.HttpExceptionUtils

import scala.concurrent.Future

trait Service {

  protected final def error[T <: HttpException](obj: T): Future[Left[T, Nothing]] = HttpExceptionUtils.error(obj)

}