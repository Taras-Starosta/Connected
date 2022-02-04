package com.scalamandra.server.impl

import com.scalamandra.config.DbConfig
import com.scalamandra.server.Migrations
import com.scalamandra.server.Migrations._
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateErrorResult

import scala.concurrent.Future
import scala.util.control.NonFatal

class MigrationsImpl(config: DbConfig) extends Migrations {
  import config._

  override def start(): Future[Unit] =
    if(config.migrations) {
      scribe.info("Starting db migrations")
      try {
        val result = Flyway.configure()
          .dataSource(
            url,
            user,
            password
          ).load()
          .migrate()
        if(result.success) Future.unit
        else failedMigration(result.asInstanceOf[MigrateErrorResult])
      } catch {
        case NonFatal(exc) =>
          failedMigration(exc)
      }
    } else Future.unit

}