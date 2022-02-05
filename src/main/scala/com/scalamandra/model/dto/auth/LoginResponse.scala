package com.scalamandra.model.dto.auth

import com.scalamandra.serialization._

case class LoginResponse(
                          authToken: String,
                          refreshToken: String,
                        )
object LoginResponse {

  implicit val readWriter: ReadWriter[LoginResponse] = macroRW[LoginResponse]

}