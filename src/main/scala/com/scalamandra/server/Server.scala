package com.scalamandra.server

import scala.concurrent.Future

trait Server {

  def start(): Future[Unit]

}