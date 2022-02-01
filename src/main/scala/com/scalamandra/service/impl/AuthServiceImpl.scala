package com.scalamandra.service.impl

import com.scalamandra.dao.UserDao
import com.scalamandra.integration.Mailer
import com.scalamandra.model.dto.RegisterRequest
import com.scalamandra.service.AuthService

import scala.concurrent.{ExecutionContext, Future}

class AuthServiceImpl(
                       userDao: UserDao,
                       mailer: Mailer,
                     )(implicit ec: ExecutionContext) extends AuthService {

  override def register(request: RegisterRequest): Future[Unit] =
    for {
      maybeUser <- userDao.getByEmail(request.email)
    }

}