package com.scalamandra.service

import com.scalamandra.model.HttpException.Conflict
import com.scalamandra.model.dto.RegisterRequest

import scala.concurrent.Future

trait AuthService {

  def register(request: RegisterRequest): Future[Either[Conflict, Unit]]

}