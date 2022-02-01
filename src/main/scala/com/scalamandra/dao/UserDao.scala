package com.scalamandra.dao

import com.scalamandra.model.db.User

import scala.concurrent.Future

trait UserDao {

  def getByEmail(email: String): Future[Option[User]]

  def create(email: String, password: String): Future[User]

}