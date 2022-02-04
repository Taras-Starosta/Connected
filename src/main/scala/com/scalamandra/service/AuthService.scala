package com.scalamandra.service

import com.scalamandra.model.HttpException.{Conflict, Unauthorized}
import com.scalamandra.model.dto.{LoginRequest, LoginResponse, RegisterRequest}

import scala.concurrent.Future

trait AuthService extends Service {

  def register(request: RegisterRequest): Future[Either[Conflict, Unit]]

  def login(request: LoginRequest): Future[Either[Unauthorized, LoginResponse]]

}