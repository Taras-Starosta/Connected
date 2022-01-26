package com.scalamandra.controller

import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

trait Controller {

  type Endpoint = ServerEndpoint[AkkaStreams with WebSockets, Future]

  def endpoints: List[Endpoint]

}