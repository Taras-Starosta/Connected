package com.scalamandra.server

import akka.http.scaladsl.Http.ServerBinding

import scala.concurrent.Future

trait Server {

  def start(): Future[ServerBinding]

}