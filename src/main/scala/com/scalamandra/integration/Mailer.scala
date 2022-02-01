package com.scalamandra.integration

import com.scalamandra.model.db.Token

import scala.concurrent.Future

trait Mailer {

  def sendConfirmation(token: Token): Future[Unit]

}