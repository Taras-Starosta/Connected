package com.scalamandra.config

import pureconfig.ConfigReader
import pureconfig.generic.semiauto._

case class DatabaseConfig(
                           driver:   String,
                           url:      String,
                           user:     String,
                           password: String,
                           migrations: Boolean,
                         )
object DatabaseConfig {

  implicit val reader: ConfigReader[DatabaseConfig] = deriveReader[DatabaseConfig]

}