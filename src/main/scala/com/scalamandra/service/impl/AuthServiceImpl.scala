package com.scalamandra.service.impl

import akka.http.scaladsl.util.FastFuture
import com.scalamandra.dao.{TokenDao, UserDao}
import com.scalamandra.integration.Mailer
import com.scalamandra.model.HttpException.{Conflict, UserAlreadyExists}
import com.scalamandra.model.dto.RegisterRequest
import com.scalamandra.provider.TokenProvider
import com.scalamandra.service.AuthService

import scala.concurrent.{ExecutionContext, Future}

class AuthServiceImpl(
                       userDao: UserDao,
                       mailer: Mailer,
                       tokenProvider: TokenProvider,
                       tokenDao: TokenDao,
                     )(implicit ec: ExecutionContext) extends AuthService {

  override def register(request: RegisterRequest): Future[Either[Conflict, Unit]] =
    for {
      maybeUser <- userDao.getByEmail(request.email)
      result <- maybeUser match {
        case Some(_) =>
          FastFuture.successful(
            Left(UserAlreadyExists)
          )
        case None =>
          for {
            _ <- userDao.create(
              nickname = request.nickname,
              email = request.email,
              password = request.password,
            )
            tokenBody = tokenProvider.generateToken
            token <- tokenDao.create(tokenBody)
            _ <- mailer.sendConfirmation(token)
          } yield Right(())
      }
    } yield result

}