package com.scalamandra.model.dto

import com.scalamandra.serialization._

case class LoginRequest(
                         email: String,
                         password: String,
                       )
object LoginRequest {

  implicit val readWriter: ReadWriter[LoginRequest] = macroRW[LoginRequest]

}