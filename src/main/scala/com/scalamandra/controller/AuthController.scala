package com.scalamandra.controller

import com.scalamandra.config.AuthConfig
import com.scalamandra.model.HttpException.{Conflict, UserAlreadyExists}
import com.scalamandra.model.dto.RegisterRequest
import com.scalamandra.service.AuthService
import sttp.tapir._
import sttp.tapir.generic.auto._

import java.time.Clock

class AuthController(
                      val authConfig: AuthConfig,
                      authService: AuthService,
                    )
                    (implicit val clock: Clock) extends AuthedController {

  override def endpoints: List[Endpoint] =
    List(register)

  def register: Endpoint =
    endpoint.put
      .description("Registration endpoint")
      .errorOut(oneOf[Conflict](oneOfHttp(UserAlreadyExists)))
      .in("api" / "v1" / "auth" / "register")
      .in(jsonBody[RegisterRequest])
      .serverLogic(authService.register)

}