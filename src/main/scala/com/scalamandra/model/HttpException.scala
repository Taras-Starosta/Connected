package com.scalamandra.model

import sttp.model.StatusCode

sealed abstract class HttpException(
                                     val statusCode: StatusCode,
                                     message: String,
                                     reason: Throwable = null.asInstanceOf[Throwable],
                                   ) extends Exception(message, reason)
object HttpException extends ExceptionCompanion[HttpException] {

  sealed abstract class Unauthorized(
                                      message: String,
                                      reason: Throwable = null.asInstanceOf[Throwable],
                                    ) extends HttpException(StatusCode.Unauthorized, message, reason)

  case object InvalidJwt extends Unauthorized("Invalid jwt.")

  case object InvalidCredentials extends Unauthorized("invalid credentials.")

  sealed abstract class NotFound(what: String) extends HttpException(StatusCode.NotFound, s"$what not found.")

  case object UserNotFound extends NotFound("User")

  case object ConfirmationNotFound extends NotFound("Pending confirmation")

  sealed abstract class Conflict(reason: String) extends HttpException(StatusCode.Conflict, reason)

  case object UserAlreadyExists extends Conflict("User already exists.")

}