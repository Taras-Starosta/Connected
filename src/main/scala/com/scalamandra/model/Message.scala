package com.scalamandra.model

import com.scalamandra.serialization._

import java.time.Instant

case class Message(
                    id: String,
                    payload: String,
                    author: Long,
                    timestamp: Instant,
                  )
object Message {

  implicit val readWriter: ReadWriter[Message] = macroRW[Message]

}