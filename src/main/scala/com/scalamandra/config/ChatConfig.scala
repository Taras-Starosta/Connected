package com.scalamandra.config

import akka.util.Timeout
import pureconfig.ConfigReader
import pureconfig.generic.semiauto._

import scala.concurrent.duration.FiniteDuration

case class ChatConfig(
                       responseTimeout: FiniteDuration,
                     ) {

  implicit val akkaTimeout: Timeout = Timeout(responseTimeout)

}
object ChatConfig {

  implicit val reader: ConfigReader[ChatConfig] = deriveReader[ChatConfig]

}