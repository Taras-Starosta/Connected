package com.scalamandra.model.db

import java.time.Instant

case class Token(
                  body: String,
                  userId: Long,
                  expiredAt: Instant,
                )