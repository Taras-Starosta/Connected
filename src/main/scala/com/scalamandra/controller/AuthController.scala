package com.scalamandra.controller

import com.scalamandra.config.ApiConfig
import com.scalamandra.model.HttpException.{Conflict, InvalidCredentials, Unauthorized, UserAlreadyExists}
import com.scalamandra.model.dto.{LoginRequest, LoginResponse, RegisterRequest}
import com.scalamandra.service.AuthService
import sttp.tapir._
import sttp.tapir.generic.auto._

class AuthController(
                      val apiConfig: ApiConfig,
                      authService: AuthService,
                    ) extends Controller {

  override def endpoints: List[Endpoint] =
    List(register)

  val basePath: EndpointInput[Unit] = version / "auth"

  def register: Endpoint =
    endpoint.put
      .description("Registration")
      .errorOut(oneOf[Conflict](oneOfHttp(UserAlreadyExists)))
      .in(basePath / "register")
      .in(jsonBody[RegisterRequest])
      .serverLogic(authService.register)

  def login: Endpoint =
    endpoint.post
      .description("Login")
      .errorOut(oneOf[Unauthorized](oneOfHttp(InvalidCredentials)))
      .in(basePath / "login")
      .in(jsonBody[LoginRequest])
      .out(jsonBody[LoginResponse])
      .serverLogic(authService.login)

}