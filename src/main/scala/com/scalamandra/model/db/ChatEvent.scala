package com.scalamandra.model.db

import com.scalamandra.model.dto.auth.AuthedUser

sealed trait ChatEvent
object ChatEvent {

  case class JoinedUser(user: AuthedUser) extends ChatEvent

  case class LeftUser(id: Long) extends ChatEvent

  case class NewMessage(message: Message) extends ChatEvent

  case class DeletedMessage(id: String) extends ChatEvent

}