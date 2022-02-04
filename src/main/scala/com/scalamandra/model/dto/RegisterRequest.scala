package com.scalamandra.model.dto

import com.scalamandra.serialization._

case class RegisterRequest(
                            email: String,
                            nickname: String,
                            password: String,
                          )
object RegisterRequest {

  implicit val reader: ReadWriter[RegisterRequest] = macroRW[RegisterRequest]

}