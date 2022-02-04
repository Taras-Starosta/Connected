package com.scalamandra.provider

import com.scalamandra.model.HttpException.Unauthorized
import com.scalamandra.model.db.User
import com.scalamandra.model.dto.AuthedUser
import sttp.tapir.server.PartialServerEndpoint

trait AuthProvider[F[_], AuthInput] {

  def authed: PartialServerEndpoint[AuthInput, AuthedUser, Unit, Unauthorized, Unit, Any, F]

  def releaseAuth(user: User): String

}