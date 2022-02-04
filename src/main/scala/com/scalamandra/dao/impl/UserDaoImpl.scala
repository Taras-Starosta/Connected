package com.scalamandra.dao.impl

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.scalamandra.dao.UserDao
import com.scalamandra.model.db.User
import doobie._
import doobie.implicits._
import com.github.dwickern.macros.NameOf._

import scala.concurrent.Future

class UserDaoImpl(val xa: Transactor[IO])
                 (implicit val ioRuntime: IORuntime)
  extends UserDao with DoobieDao {

  val idField: String = allCaps(nameOf[User](_.id))
  val nickField: String = allCaps(nameOf[User](_.nickname))
  val emailField: String = allCaps(nameOf[User](_.email))
  val passField: String = allCaps(nameOf[User](_.password))
  val avatarField: String = allCaps(nameOf[User](_.avatarUrl))
  val activeField: String = allCaps(nameOf[User](_.active))

  val allFields: Seq[String] = Vector(
    idField,
    nickField,
    emailField,
    passField,
    avatarField,
    activeField,
  )

  override def getByEmail(email: String): Future[Option[User]] =
    transact {
      sql"select $email from users"
        .query[User]
        .option
    }

  override def create(nickname: String, email: String, password: String): Future[User] =
    insert[User](allFields) {
      sql"insert into users($idField, $emailField, $passField) values($nickname, $email, $password)"
    }

}