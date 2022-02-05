package com.scalamandra.config

import pureconfig.ConfigReader
import pureconfig.generic.semiauto._

case class DbConfig(
                     driver: String,
                     url: String,
                     user: String,
                     password: String,
                     migrations: Boolean,
                   )
object DbConfig {

  implicit val reader: ConfigReader[DbConfig] = deriveReader[DbConfig]

}