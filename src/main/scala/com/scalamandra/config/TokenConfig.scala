package com.scalamandra.config

import pureconfig.ConfigReader
import pureconfig.generic.semiauto._

import scala.concurrent.duration.FiniteDuration

case class TokenConfig(
                        ttl: FiniteDuration,
                      )
object TokenConfig {

  implicit val reader: ConfigReader[TokenConfig] = deriveReader[TokenConfig]

}