package com.scalamandra.config

import pureconfig.ConfigReader
import pureconfig.generic.semiauto._

import scala.concurrent.duration.FiniteDuration

case class AuthConfig(
                       jwtTtl: FiniteDuration,
                       apiKeyTtl: FiniteDuration,
                       appSecret: String,
                     )
object AuthConfig {

  implicit val reader: ConfigReader[AuthConfig] = deriveReader[AuthConfig]

}