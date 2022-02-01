package com.scalamandra.service

import com.scalamandra.model.dto.RegisterRequest

import scala.concurrent.Future

trait AuthService {

  def register(request: RegisterRequest): Future[Unit]

}