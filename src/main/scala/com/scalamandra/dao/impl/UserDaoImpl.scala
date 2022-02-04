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

  val idField: String = fieldName(nameOf[User](_.id))
  val nickField: String = fieldName(nameOf[User](_.nickname))
  val emailField: String = fieldName(nameOf[User](_.email))
  val passField: String = fieldName(nameOf[User](_.password))
  val avatarField: String = fieldName(nameOf[User](_.avatarUrl))
  val activeField: String = fieldName(nameOf[User](_.active))

  val allFields: Seq[String] = Vector(
    idField,
    nickField,
    emailField,
    passField,
    avatarField,
    activeField,
  )
  
  val table: String = tableName(nameOfType[User])

  override def getByEmail(email: String): Future[Option[User]] =
    transact {
      sql"select $email from $table"
        .query[User]
        .option
    }

  override def create(nickname: String, email: String, password: String): Future[User] =
    insert[User](allFields) {
      sql"insert into $table($idField, $emailField, $passField) values($nickname, $email, $password)"
    }

}