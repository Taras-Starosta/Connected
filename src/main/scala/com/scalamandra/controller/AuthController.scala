package com.scalamandra.controller

import com.scalamandra.config.ApiConfig
import com.scalamandra.model.HttpException.{Conflict, InvalidCredentials, Unauthorized, UserAlreadyExists}
import com.scalamandra.model.dto.{LoginRequest, LoginResponse, RefreshRequest, RefreshResponse, RegisterRequest}
import com.scalamandra.provider.AuthProvider
import com.scalamandra.service.AuthService
import pdi.jwt.JwtClaim
import sttp.tapir._
import sttp.tapir.generic.auto._

import scala.concurrent.Future

class AuthController(
                      val apiConfig: ApiConfig,
                      authService: AuthService,
                      authProvider: AuthProvider[Future, JwtClaim]
                    ) extends Controller {

  override def endpoints: List[Endpoint] =
    List(
      register,
      login,
      refresh,
    )

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

  def refresh: Endpoint =
    authProvider.authed(
      oneOfHttp(InvalidCredentials)
    ).post
      .description("Refresh jwt session")
      .in(basePath / "refresh")
      .in(jsonBody[RefreshRequest])
      .out(jsonBody[RefreshResponse])
      .serverLogic { u => r =>
        authService.refresh(u, r)
      }

}