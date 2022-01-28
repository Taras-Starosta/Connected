package com.scalamandra.model

import com.scalamandra.serialization._

case class AuthedUser(
                       email: String,
                     )
object AuthedUser {

  implicit val readWriter: ReadWriter[AuthedUser] = macroRW[AuthedUser]

}