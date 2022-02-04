package com.scalamandra.config

import pureconfig.ConfigReader
import pureconfig.generic.semiauto._

case class BCryptConfig(
                         rounds: Int,
                       )
object BCryptConfig {

  implicit val reader: ConfigReader[BCryptConfig] = deriveReader[BCryptConfig]

}