package com.scalamandra.config

import pureconfig.ConfigReader
import pureconfig.generic.semiauto._

case class EmailConfig(
                        user: String,
                        password: String,
                        host: String,
                        port: Int,
                        from: String,
                      )
object EmailConfig {

  implicit val emailReader: ConfigReader[EmailConfig] = deriveReader[EmailConfig]

}