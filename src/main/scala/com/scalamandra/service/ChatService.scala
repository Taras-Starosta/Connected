package com.scalamandra.service

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.scalamandra.model.WsException
import com.scalamandra.model.dto.auth.AuthedUser
import com.scalamandra.model.dto.chat.ChatProtocol.{ClientMessage, ServerMessage}

import scala.concurrent.Future

trait ChatService extends Service {

  def handle(user: AuthedUser): Future[Either[WsException, Flow[ClientMessage, ServerMessage, NotUsed]]]

}
