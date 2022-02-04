package com.scalamandra.dao

import com.scalamandra.model.db.{Token, User}

import scala.concurrent.Future

trait TokenDao {

  def create(user: User, body: String): Future[Token]

}