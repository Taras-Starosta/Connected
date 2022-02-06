package com.scalamandra.provider

import com.scalamandra.model.db.User
import com.scalamandra.model.dto.auth.AuthedUser
import com.scalamandra.model.{HttpException, WsException}
import sttp.tapir.EndpointOutput.OneOfVariant
import sttp.tapir.server.PartialServerEndpoint

trait AuthProvider[F[_], AuthInput] {

  def httpAuthed(errorOut: OneOfVariant[_ <: HttpException]*): PartialServerEndpoint[AuthInput, AuthedUser, Unit, HttpException, Unit, Any, F]

  def wsAuthed(errorOut: OneOfVariant[_ <: WsException]*): PartialServerEndpoint[String, AuthedUser, Unit, WsException, Unit, Any, F]

  def releaseJwt(user: User): F[String]

  def releaseJwt(user: AuthedUser): F[String]

  def releaseApiKey(user: User): F[String]

  def releaseApiKey(user: AuthedUser): F[String]

}