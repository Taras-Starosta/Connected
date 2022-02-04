package com.scalamandra.integration

import com.scalamandra.model.db.{Token, User}

import scala.concurrent.Future

trait Mailer {

  def sendConfirmation(user: User, token: Token): Future[Unit]

}