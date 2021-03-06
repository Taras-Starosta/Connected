package com.scalamandra.service.impl

import com.scalamandra.dao.{TokenDao, UserDao}
import com.scalamandra.integration.Mailer
import com.scalamandra.model.HttpException
import com.scalamandra.model.HttpException.{ConfirmationNotFound, Conflict, InvalidCredentials, Unauthorized, UserAlreadyExists}
import com.scalamandra.model.db.User
import com.scalamandra.model.dto.auth._
import com.scalamandra.provider.{AuthProvider, BCryptProvider, TokenProvider}
import com.scalamandra.service.AuthService
import pdi.jwt.JwtClaim

import scala.concurrent.{ExecutionContext, Future}

class AuthServiceImpl(
                       mailer: Mailer,
                       userDao: UserDao,
                       tokenDao: TokenDao,
                       tokenProvider: TokenProvider,
                       bcryptProvider: BCryptProvider,
                       authProvider: AuthProvider[Future, JwtClaim],
                     )(implicit ec: ExecutionContext) extends AuthService {

  override def register(request: RegisterRequest): Future[Either[Conflict, Unit]] =
    for {
      maybeUser <- userDao.getByEmail(request.email)
      result <- maybeUser match {
        case Some(_) =>
          error(UserAlreadyExists)
        case None =>
          for {
            user <- userDao.create(
              nickname = request.nickname,
              email = request.email,
              password = bcryptProvider.encrypt(request.password),
            )
            tokenBody = tokenProvider.generateToken
            token <- tokenDao.createConfirmation(user, tokenBody)
            _ = mailer.sendConfirmation(user, token)
          } yield Right(())
      }
    } yield result

  override def login(request: LoginRequest): Future[Either[Unauthorized, LoginResponse]] = {

    def isValid(user: User): Boolean = user.active && bcryptProvider.compare(request.password, user.password)

    for {
      maybeUser <- userDao.getByEmail(request.email)
      result <- maybeUser match {
        case Some(user) if isValid(user) =>
          val tokenBody = tokenProvider.generateToken
          for {
            refreshToken <- tokenDao.createRefresh(user, tokenBody)
            authToken <- authProvider.releaseJwt(user)
            response = LoginResponse(
              authToken = authToken,
              refreshToken = refreshToken.body,
            )
          } yield Right(response)

        case _ => error(InvalidCredentials)
      }
    } yield result
  }

  override def refresh(authedUser: AuthedUser, request: RefreshRequest): Future[Either[Unauthorized, RefreshResponse]] =
    for {
      success <- tokenDao.refresh(authedUser.id, request.refreshToken)
      jwt <- authProvider.releaseJwt(authedUser)
    } yield if(success) {
      Right(
        RefreshResponse(
          jwt
        )
      )
    } else Left(InvalidCredentials)

  override def activate(request: ActivationRequest): Future[Either[HttpException.NotFound, Unit]] =
    for {
      success <- userDao.activate(request.userId, request.token)
    } yield if(success) Right(())
      else Left(ConfirmationNotFound)

  override def apiKey(user: AuthedUser, maybeIp: Option[String]): Future[Either[HttpException, ApiKey]] =
    maybeIp match {
      case Some(ip) =>
        for {
          body <- authProvider.releaseApiKey(user, ip)
          key = ApiKey(body)
        } yield Right(key)
      case None =>
        error(InvalidCredentials)
    }

}