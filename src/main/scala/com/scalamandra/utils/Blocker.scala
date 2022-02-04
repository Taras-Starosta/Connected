package com.scalamandra.utils

import akka.actor.typed.{ActorSystem, DispatcherSelector}

import scala.concurrent.{ExecutionContext, Future}

trait Blocker {

  implicit def actorSystem: ActorSystem[_]

  private lazy val blocker: ExecutionContext = Blocker.blockingEC

  def blocking[T](operation: => T): Future[T] = Future {
    scala.concurrent.blocking(operation)
  }(blocker)

}
object Blocker {

  def blockingEC(implicit actorSystem: ActorSystem[_]): ExecutionContext =
    actorSystem.dispatchers.lookup(DispatcherSelector.blocking())

}