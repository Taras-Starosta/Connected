package com.scalamandra.dao.impl

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.github.dwickern.macros.NameOf._
import com.scalamandra.config.TokenConfig
import com.scalamandra.dao.TokenDao
import com.scalamandra.model.db.Token.{ConfirmationToken, RefreshToken}
import com.scalamandra.model.db.{Token, User}
import doobie._
import doobie.implicits._

import java.time.{Clock, Instant}
import scala.concurrent.{ExecutionContext, Future}

class TokenDaoImpl(
                    val xa: Transactor[IO],
                    tokenConfig: TokenConfig,
                  )(implicit
                    val ioRuntime: IORuntime,
                    clock: Clock,
                    ec: ExecutionContext,
                  )
  extends TokenDao with DoobieDao {
  import tokenConfig._

  val bodyField: String = fieldName(nameOf[Token](_.body))
  val userField: String = fieldName(nameOf[Token](_.userId))
  val expiredField: String = fieldName(nameOf[ConfirmationToken](_.expiredAt))

  val refreshFields: Seq[String] = Vector(
    bodyField,
    userField,
  )

  val confirmationFields: Seq[String] = Vector(
    bodyField,
    userField,
    expiredField,
  )

  val table: String = tableName(nameOfType[Token])

  def expiredAt: Instant =
    Instant.now(clock).plus(
      confirmationTtl.length,
      confirmationTtl.unit.toChronoUnit,
    )

  override def createConfirmation(user: User, body: String): Future[ConfirmationToken] =
    insert[ConfirmationToken](confirmationFields) {
      sql"insert into $table($bodyField, $userField, $expiredField) values($body, ${user.id}, $expiredAt)"
    }

  override def confirm(userId: Long, body: String): Future[Boolean] =
    for {
      affected <- update {
        sql"delete from $table where $bodyField=$body and $userField=$userId"
      }
    } yield affected > 0

  override def createRefresh(user: User, body: String): Future[RefreshToken] =
    for {
      _ <- update {
        sql"delete from $table where $userField=${user.id}"
      }
      result <- insert[RefreshToken](refreshFields) {
        sql"insert into $table($bodyField, $userField) values($body, ${user.id})"
      }
    } yield result

  override def refresh(userId: Long, body: String): Future[Boolean] =
    for {
      maybeToken <- selectOne[RefreshToken] {
        sql"select $bodyField, $userField from $table where $bodyField=$body and $userField=$userId"
      }
    } yield maybeToken.nonEmpty

}