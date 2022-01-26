package com.scalamandra.config

import pureconfig.ConfigReader
import pureconfig.generic.semiauto._

case class ApiConfig(
                      name: String,
                      version: String,
                      swagger: Boolean,
                    )
object ApiConfig {

  implicit val reader: ConfigReader[ApiConfig] = deriveReader[ApiConfig]

}