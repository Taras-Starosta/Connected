package com.scalamandra.model

import com.scalamandra.serialization._

case class AuthedUser(
                       id: Long,
                       nickname: String,
                       email: String,
                       avatarUrl: Option[String],
                     )
object AuthedUser {

  implicit val readWriter: ReadWriter[AuthedUser] = macroRW[AuthedUser]

}