package com.scalamandra.service

import com.scalamandra.model.HttpException.{Conflict, NotFound, Unauthorized}
import com.scalamandra.model.dto.auth._

import scala.concurrent.Future

trait AuthService extends Service {

  def register(request: RegisterRequest): Future[Either[Conflict, Unit]]

  def login(request: LoginRequest): Future[Either[Unauthorized, LoginResponse]]

  def refresh(authedUser: AuthedUser, request: RefreshRequest): Future[Either[Unauthorized, RefreshResponse]]

  def activate(request: ActivationRequest): Future[Either[NotFound, Unit]]

}