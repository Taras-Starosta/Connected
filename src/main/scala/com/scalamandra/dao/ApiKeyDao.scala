package com.scalamandra.dao

import com.scalamandra.model.dto.auth.AuthedUser

import scala.concurrent.Future

trait ApiKeyDao {

  def release(authedUser: AuthedUser): Future[String]

  def validate(key: String): Future[Option[AuthedUser]]

}