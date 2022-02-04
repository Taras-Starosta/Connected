package com.scalamandra.dao.impl

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import doobie._
import doobie.implicits._

import scala.annotation.tailrec
import scala.concurrent.Future

trait DoobieDao {

  val xa: Transactor[IO]

  implicit val ioRuntime: IORuntime

  protected final def allCaps(camelCase: String): String = {
    val screamingSnake = new StringBuilder()
    @tailrec
    def loop(i: Int): Unit =
      if(i < camelCase.length) {
        val current = camelCase(i)
        if(current.isUpper && i != 0) {
          screamingSnake += '_'
          screamingSnake += current
        } else {
          screamingSnake += current.toUpper
        }
        loop(i + 1)
      }
    loop(0)
    screamingSnake.result()
  }

  protected final def transact[T](body: => ConnectionIO[T]): Future[T] =
    body.transact(xa).unsafeToFuture

  protected final def insert[T: Read](fields: Seq[String])(sql: => Fragment): Future[T] =
    transact {
      sql.update.withUniqueGeneratedKeys(fields: _*)
    }

}