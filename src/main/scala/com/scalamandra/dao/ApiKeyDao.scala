package com.scalamandra.dao

import com.scalamandra.model.dto.auth.AuthedUser

import scala.concurrent.Future

trait ApiKeyDao {

  def release(authedUser: AuthedUser, ip: String): Future[String]

  def validate(key: String, ip: String): Future[Option[AuthedUser]]

}