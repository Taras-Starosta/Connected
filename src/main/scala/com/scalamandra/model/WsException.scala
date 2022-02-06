package com.scalamandra.model

abstract class WsException(message: String) extends Exception(message)
object WsException extends ExceptionCompanion[WsException] {

  case object InvalidApiKey extends WsException("Invalid api key.")

}