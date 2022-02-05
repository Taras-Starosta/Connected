package com.scalamandra.model.db

import java.time.Instant

trait Token {

  def body: String

  def userId: Long

}
object Token {

  case class ConfirmationToken(
                                body: String,
                                userId: Long,
                                expiredAt: Instant,
                              ) extends Token

  case class RefreshToken(
                           body: String,
                           userId: Long,
                         ) extends Token

}