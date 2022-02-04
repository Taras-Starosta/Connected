package com.scalamandra.service

import com.scalamandra.model.HttpException

import scala.concurrent.Future

trait Service {

  protected final def error[T <: HttpException](obj: T): Future[Left[T, Nothing]] = HttpException.error(obj)

}