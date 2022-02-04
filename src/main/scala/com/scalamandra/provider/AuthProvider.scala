package com.scalamandra.provider

import com.scalamandra.model.HttpException
import com.scalamandra.model.db.User
import com.scalamandra.model.dto.AuthedUser
import sttp.tapir.EndpointOutput.OneOfVariant
import sttp.tapir.server.PartialServerEndpoint

trait AuthProvider[F[_], AuthInput] {

  def authed(errorOut: OneOfVariant[_ <: HttpException]*): PartialServerEndpoint[AuthInput, AuthedUser, Unit, HttpException, Unit, Any, F]

  def releaseAuth(user: User): String

  def releaseAuth(user: AuthedUser): String

}