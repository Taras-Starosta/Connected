package com.scalamandra.config

import akka.util.Timeout

import scala.concurrent.duration.FiniteDuration

import pureconfig.ConfigReader
import pureconfig.generic.semiauto._

case class ChatConfig(
                       responseTimeout: FiniteDuration,
                     ) {

  implicit val akkaTimeout: Timeout = Timeout(responseTimeout)

}
object ChatConfig {

  implicit val reader: ConfigReader[ChatConfig] = deriveReader[ChatConfig]

}