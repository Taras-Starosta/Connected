package com.scalamandra.dao

import com.scalamandra.model.db.Token

import scala.concurrent.Future

trait TokenDao {

  def create(body: String): Future[Token]

}