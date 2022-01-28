package com.scalamandra.controller

import com.scalamandra.serialization.uPickleTapir
import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

trait Controller extends uPickleTapir {

  type Endpoint = ServerEndpoint[AkkaStreams with WebSockets, Future]

  def endpoints: List[Endpoint]

}