package com.scalamandra.dao

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.scalamandra.dao.ChatDao.ChatSubscription
import com.scalamandra.model.db.{ChatEvent, Message}
import com.scalamandra.model.dto.auth.AuthedUser

import scala.concurrent.Future

trait ChatDao {

  def subscribe(user: AuthedUser): Future[ChatSubscription]

  def unsubscribe(id: Long): Future[Unit]

  def addMessage(payload: String, author: AuthedUser): Future[Unit]

  def removeMessage(id: String, user: AuthedUser): Future[Unit]

}
object ChatDao {

  case class ChatSubscription(
                               messages: Iterable[Message],
                               users: Iterable[AuthedUser],
                               events: Source[ChatEvent, NotUsed],
                             )

}