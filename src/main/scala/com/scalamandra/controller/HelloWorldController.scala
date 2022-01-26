package com.scalamandra.controller
import akka.http.scaladsl.util.FastFuture
import sttp.tapir._

import scala.concurrent.Future

class HelloWorldController extends Controller {

  val endpoints = List(
    helloWorld
  )

  def helloWorld =
    endpoint.get
      .description("Hello world!")
      .in("hello" / path[String])
      .out(stringBody)
      .serverLogic[Future] { name =>
        FastFuture.successful(Right(s"Hello, $name!"))
      }
}