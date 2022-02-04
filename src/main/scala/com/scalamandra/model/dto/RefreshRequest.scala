package com.scalamandra.model.dto

import com.scalamandra.serialization._

case class RefreshRequest(
                           refreshToken: String,
                         )
object RefreshRequest {

  implicit val readWriter: ReadWriter[RefreshRequest] = macroRW[RefreshRequest]

}