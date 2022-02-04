package com.scalamandra.model.dto.auth

import com.scalamandra.serialization._

case class RefreshResponse(
                            authToken: String,
                          )
object RefreshResponse {

  implicit val readWriter: ReadWriter[RefreshResponse] = macroRW[RefreshResponse]

}