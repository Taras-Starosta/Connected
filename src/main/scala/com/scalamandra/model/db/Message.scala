package com.scalamandra.model.db

import java.time.ZonedDateTime

case class Message(
                    id: String,
                    payload: String,
                    author: Long,
                    timestamp: ZonedDateTime,
                  )