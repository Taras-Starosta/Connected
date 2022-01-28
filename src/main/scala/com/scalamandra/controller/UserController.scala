package com.scalamandra.controller

import com.scalamandra.config.AuthConfig
import sttp.tapir._

import java.time.Clock

class UserController(val authConfig: AuthConfig)
                    (implicit val clock: Clock) extends AuthedController {

  override def endpoints: List[Endpoint] = ???

  def getUser: Endpoint =
    authed.get
      .in("api" / "v1" / "users")
      .description("Get current user")
      .out(stringBody)
      .serverLogic { user =>
        println(user)
      }


}