package com.scalamandra.dao.impl

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.scalamandra.config.TokenConfig
import com.scalamandra.dao.TokenDao
import com.scalamandra.model.db.Token.{ConfirmationToken, RefreshToken}
import com.scalamandra.model.db.User
import doobie._
import doobie.implicits._
import doobie.implicits.legacy.instant._ //DO NOT DELETE

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

  def expiredAt(): Instant =
    Instant.now(clock).plus(
      confirmationTtl.length,
      confirmationTtl.unit.toChronoUnit,
    )

  override def createConfirmation(user: User, body: String): Future[ConfirmationToken] =
    transact {
      val expired = expiredAt()
      for {
        _ <- sql"insert into tokens(body, user_id, expired_at) values($body, ${user.id}, $expired)".update.run
        result <- sql"select * from tokens where body=$body".query[ConfirmationToken].unique
      } yield result
    }

  override def confirm(userId: Long, body: String): Future[Boolean] =
    for {
      affected <- update {
        sql"delete from tokens where body=$body and user_id=$userId"
      }
    } yield affected > 0

  override def createRefresh(user: User, body: String): Future[RefreshToken] =
    transact {
      for {
        _ <- sql"delete from tokens where user_id=${user.id}".update.run
        _ <- sql"insert into tokens(body, user_id) values($body, ${user.id})".update.run
        result <- sql"select body, user_id from tokens where body=$body".query[RefreshToken].unique
      } yield result
    }

  override def refresh(userId: Long, body: String): Future[Boolean] =
    for {
      maybeToken <- selectOne[RefreshToken] {
        sql"select body, user_id from tokens where body=$body and user_id=$userId"
      }
    } yield maybeToken.nonEmpty

}