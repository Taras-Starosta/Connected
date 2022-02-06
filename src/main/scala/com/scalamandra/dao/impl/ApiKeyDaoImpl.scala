package com.scalamandra.dao.impl

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.github.benmanes.caffeine.cache.Caffeine
import com.scalamandra.config.AuthConfig
import com.scalamandra.dao.ApiKeyDao
import com.scalamandra.model.dto.auth.AuthedUser
import com.scalamandra.provider.TokenProvider
import scalacache.Flags.defaultFlags
import scalacache._
import scalacache.caffeine._

import scala.concurrent.Future

class ApiKeyDaoImpl(tokenProvider: TokenProvider,
                    authConfig: AuthConfig)
                   (implicit ioRuntime: IORuntime) extends ApiKeyDao {

  case class WsSession(user: AuthedUser, ip: String)

  implicit val cache: CaffeineCache[IO, String, WsSession] = {
    val (ttl, unit) = {
      val config = authConfig.apiKeyTtl
      config.length -> config.unit
    }
    val underlying = Caffeine.newBuilder()
      .expireAfterWrite(ttl, unit)
      .build[String, Entry[WsSession]]()
    CaffeineCache[IO, String, WsSession](underlying)
  }

  override def release(authedUser: AuthedUser, ip: String): Future[String] = {
    val apiKey = tokenProvider.generateToken
    val wsSession = WsSession(authedUser, ip)
    put(apiKey)(wsSession)
      .as(apiKey)
      .unsafeToFuture()
  }

  override def validate(key: String, ip: String): Future[Option[AuthedUser]] =
    get(key).map { maybeSession =>
      for {
        session <- maybeSession
        if session.ip == ip
      } yield session.user
    }.unsafeToFuture()

}