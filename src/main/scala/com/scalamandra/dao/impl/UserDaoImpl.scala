package com.scalamandra.dao.impl

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.github.dwickern.macros.NameOf._
import com.scalamandra.dao.UserDao
import com.scalamandra.model.db.User
import doobie._
import doobie.implicits._

import scala.concurrent.Future

class UserDaoImpl(val xa: Transactor[IO])
                 (implicit val ioRuntime: IORuntime)
  extends UserDao with DoobieDao {

  val key: String = fieldName(nameOf[User](_.id))
  val table: String = tableName(nameOfType[User])

  override def getByEmail(email: String): Future[Option[User]] =
    transact {
      sql"select * from users where email=$email"
        .query[User]
        .option
    }

  override def create(nickname: String, email: String, password: String): Future[User] =
    transact {
      for {
        _ <- sql"insert into users(nickname, email, password) values($nickname, $email, $password)".update.run
        id <- sql"select lastval()".query[Long].unique
        result <- sql"select * from users where id=$id".query[User].unique
      } yield result
    }

}