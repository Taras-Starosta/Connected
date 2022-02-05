package com.scalamandra.model.dto.auth

import com.scalamandra.serialization._

case class RefreshRequest(
                           refreshToken: String,
                         )
object RefreshRequest {

  implicit val readWriter: ReadWriter[RefreshRequest] = macroRW[RefreshRequest]

}