package com.scalamandra.config

import scala.concurrent.duration.FiniteDuration

import pureconfig.ConfigReader
import pureconfig.generic.semiauto._

case class AuthConfig(
                       jwtTtl: FiniteDuration,
                       appSecret: String,
                     )
object AuthConfig {

  implicit val reader: ConfigReader[AuthConfig] = deriveReader[AuthConfig]

}