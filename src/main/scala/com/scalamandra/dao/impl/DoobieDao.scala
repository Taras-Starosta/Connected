package com.scalamandra.dao.impl

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import doobie._
import doobie.implicits._
import doobie.util.fragment

import scala.annotation.tailrec
import scala.concurrent.Future

trait DoobieDao {

  val xa: Transactor[IO]

  implicit val ioRuntime: IORuntime

  protected final def fieldName(getterName: String): String = allCaps(getterName)

  protected final def tableName(typeName: String): String = s"${allCaps(typeName)}S"

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
    body.transact(xa).unsafeToFuture()

  protected final def update(sql: => Fragment): Future[Int] =
    transact {
      sql.update.run
    }

  protected final def selectOne[T: Read](sql: => Fragment): Future[Option[T]] =
    transact {
      sql.query.option
    }

}