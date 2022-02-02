package com.scalamandra.dao.impl

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.scalamandra.dao.UserDao
import com.scalamandra.model.db.User
import doobie._
import doobie.implicits._
import com.github.dwickern.macros.NameOf._

import scala.concurrent.Future

class UserDaoImpl(xa: Transactor[IO])
                 (implicit IORuntime: IORuntime) extends UserDao {

  val idField: String = nameOf[User](_.id)
  val nickField: String = nameOf[User](_.nickname)
  val emailField: String = nameOf[User](_.email)
  val passField: String = nameOf[User](_.password)
  val avatarField: String = nameOf[User](_.avatarUrl)
  val activeField: String = nameOf[User](_.active)

  val allFields: Seq[String] = Vector(
    idField,
    nickField,
    emailField,
    passField,
    avatarField,
    activeField,
  )

  override def getByEmail(email: String): Future[Option[User]] =
    sql"select $email from users"
      .query[User]
      .option
      .transact(xa)
      .unsafeToFuture

  override def create(nickname: String, email: String, password: String): Future[User] =
   sql"insert into users($idField, $emailField, $passField) values($nickname, $email, $password)"
     .update
     .withUniqueGeneratedKeys[User](allFields: _*)
     .transact(xa)
     .unsafeToFuture

}