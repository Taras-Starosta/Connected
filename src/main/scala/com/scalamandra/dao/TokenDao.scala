package com.scalamandra.dao

import com.scalamandra.model.db.Token._
import com.scalamandra.model.db.User

import scala.concurrent.Future

trait TokenDao {

  def createConfirmation(user: User, body: String): Future[ConfirmationToken]

  def confirm(userId: Long, body: String): Future[Boolean]

  def createRefresh(user: User, body: String): Future[RefreshToken]

  def refresh(userId: Long, body: String): Future[Boolean]

}