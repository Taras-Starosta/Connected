package com.scalamandra.dao.impl

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.github.dwickern.macros.NameOf._
import com.scalamandra.config.TokenConfig
import com.scalamandra.dao.TokenDao
import com.scalamandra.model.db.{Token, User}
import doobie._
import doobie.implicits._

import java.time.{Clock, Instant}
import scala.concurrent.Future

class TokenDaoImpl(val xa: Transactor[IO], tokenConfig: TokenConfig)
                  (implicit val ioRuntime: IORuntime, clock: Clock)
  extends TokenDao with DoobieDao {
  import tokenConfig._

  val bodyField: String = allCaps(nameOf[Token](_.body))
  val userField: String = allCaps(nameOf[Token](_.userId))
  val expiredField: String = allCaps(nameOf[Token](_.expiredAt))

  val allFields: Seq[String] = Vector(
    bodyField,
    userField,
    expiredField,
  )

  def expiredAt: Instant =
    Instant.now(clock).plus(
      ttl.length,
      ttl.unit.toChronoUnit,
    )

  override def create(user: User, body: String): Future[Token] =
    insert[Token](allFields) {
      sql"insert into tokens($bodyField, $userField, $expiredField) values($body, ${user.id}, $expiredAt)"
    }

}