package com.scalamandra.model.dto.auth

import com.scalamandra.serialization._

case class ApiKey(key: String)
object ApiKey {

  implicit val readWriter: ReadWriter[ApiKey] = macroRW[ApiKey]

}