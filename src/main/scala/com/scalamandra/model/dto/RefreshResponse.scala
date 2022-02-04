package com.scalamandra.model.dto

import com.scalamandra.serialization._

case class RefreshResponse(
                            authToken: String,
                          )
object RefreshResponse {

  implicit val readWriter: ReadWriter[RefreshResponse] = macroRW[RefreshResponse]

}