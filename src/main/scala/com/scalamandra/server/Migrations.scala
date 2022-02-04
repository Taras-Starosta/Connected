package com.scalamandra.server

import akka.http.scaladsl.util.FastFuture
import org.flywaydb.core.api.output.MigrateErrorResult

import scala.concurrent.Future

trait Migrations {

  def start(): Future[Unit]

}
object Migrations {

  def failedMigration(result: MigrateErrorResult): Future[Nothing] = FastFuture.failed {
    new RuntimeException(s"Migration failed: ${result.error}")
  }

  def failedMigration(exc: Throwable): Future[Nothing] = FastFuture.failed {
    new RuntimeException("Migration failed", exc)
  }

}