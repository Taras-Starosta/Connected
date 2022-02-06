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

  implicit val cache: CaffeineCache[IO, String, AuthedUser] = {
    val (ttl, unit) = {
      val config = authConfig.apiKeyTtl
      config.length -> config.unit
    }
    val underlying = Caffeine.newBuilder()
      .expireAfterWrite(ttl, unit)
      .build[String, Entry[AuthedUser]]()
    CaffeineCache[IO, String, AuthedUser](underlying)
  }

  override def release(authedUser: AuthedUser): Future[String] = {
    val apiKey = tokenProvider.generateToken
    put(apiKey)(authedUser)
      .as(apiKey)
      .unsafeToFuture()
  }

  override def validate(key: String): Future[Option[AuthedUser]] =
    get(key).unsafeToFuture()

}