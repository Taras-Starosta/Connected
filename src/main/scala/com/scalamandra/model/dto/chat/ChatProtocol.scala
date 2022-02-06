package com.scalamandra.model.dto.chat

import com.scalamandra.model.{AuthedUser, Message}
import com.scalamandra.serialization._

sealed trait ChatProtocol
object ChatProtocol {

  sealed trait ServerMessage extends ChatProtocol
  case class Snapshot(
                       messages: Iterable[Message],
                       users: Iterable[AuthedUser],
                     ) extends ServerMessage
  case class Joined(user: AuthedUser) extends ServerMessage
  case class Disconnected(id: Long) extends ServerMessage
  case class Deleted(id: String) extends ServerMessage
  case class Posted(message: Message) extends ServerMessage

  implicit val snapshotRW: ReadWriter[Snapshot] = macroRW[Snapshot]
  implicit val joinedRW: ReadWriter[Joined] = macroRW[Joined]
  implicit val disconnectedRW: ReadWriter[Disconnected] = macroRW[Disconnected]
  implicit val deletedRW: ReadWriter[Deleted] = macroRW[Deleted]
  implicit val postedRW: ReadWriter[Posted] = macroRW[Posted]
  implicit val serverRW: ReadWriter[ServerMessage] = macroRW[ServerMessage]

  sealed trait ClientMessage extends ChatProtocol
  case object Join extends ClientMessage
  case object Disconnect extends ClientMessage
  case class Post(payload: String) extends ClientMessage
  case class Delete(id: String) extends ClientMessage

  implicit val joinRW: ReadWriter[Join.type] = macroRW[Join.type]
  implicit val disconnectRW: ReadWriter[Disconnect.type] = macroRW[Disconnect.type]
  implicit val postRW: ReadWriter[Post] = macroRW[Post]
  implicit val deleteRW: ReadWriter[Delete] = macroRW[Delete]
  implicit val clientRW: ReadWriter[ClientMessage] = macroRW[ClientMessage]

}