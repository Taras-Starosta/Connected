package com.scalamandra.logging

import akka.actor.typed.{ActorSystem, DispatcherSelector}
import com.scalamandra.utils.Blocker
import scribe.{Level, Logger}
import scribe.file.FlushMode.AsynchronousFlush
import scribe.file._
import scribe.format.Formatter
import scribe.handler.{AsynchronousLogHandle, LogHandlerBuilder}
import scribe.output.format.{ANSIOutputFormat, ASCIIOutputFormat}
import scribe.writer.ConsoleWriter

object LoggerConfigurator {

  def apply()(implicit actorSystem: ActorSystem[_]): Logger =
    scribe.Logger.root
      .clearModifiers()
      .clearHandlers()
      .withMinimumLevel(Level.Debug)
      .withHandler(
        LogHandlerBuilder(
          formatter = Formatter.strict,
          writer = FileWriter(
            flushMode = AsynchronousFlush()(
              Blocker.blockingEC
            ),
            pathBuilder = "logs" / (daily() % ".log")
          ),
          outputFormat = ASCIIOutputFormat,
          handle = AsynchronousLogHandle(1024),
          modifiers = Nil,
        )
      ).withHandler(
      LogHandlerBuilder(
        formatter = Formatter.enhanced,
        writer = ConsoleWriter,
        outputFormat = ANSIOutputFormat,
        handle = AsynchronousLogHandle(1024),
        modifiers = Nil,
      )
    ).replace()

}