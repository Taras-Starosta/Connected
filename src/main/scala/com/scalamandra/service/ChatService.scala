package com.scalamandra.service

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.scalamandra.model.HttpException
import com.scalamandra.model.dto.auth.AuthedUser
import com.scalamandra.model.dto.chat.ChatProtocol.{ClientMessage, ServerMessage}

import scala.concurrent.Future

trait ChatService extends Service {

  def handle(user: AuthedUser): Future[Either[HttpException, Flow[ClientMessage, ServerMessage, NotUsed]]]

}
