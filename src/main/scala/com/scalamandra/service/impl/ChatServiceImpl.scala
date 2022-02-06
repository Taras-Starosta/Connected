package com.scalamandra.service.impl

import akka.NotUsed
import akka.actor.typed.{ActorSystem, SpawnProtocol}
import akka.http.scaladsl.util.FastFuture
import akka.stream.FlowShape
import akka.stream.scaladsl._
import com.scalamandra.dao.ChatDao
import com.scalamandra.model.WsException
import com.scalamandra.model.db.ChatEvent._
import com.scalamandra.model.dto.auth.AuthedUser
import com.scalamandra.model.dto.chat.ChatProtocol._
import com.scalamandra.service.ChatService

import scala.concurrent.{ExecutionContext, Future}

class ChatServiceImpl(chatDao: ChatDao)
                     (implicit ec: ExecutionContext,
                      actorSystem: ActorSystem[SpawnProtocol.Command]) extends ChatService {

  def none[T](body: ChatDao => Future[T]): Future[Option[Nothing]] = body(chatDao).map(_ => None)

  def some[T](obj: T): Future[Option[T]] = FastFuture.successful(Some(obj))

  override def handle(user: AuthedUser): Future[Either[WsException, Flow[ClientMessage, ServerMessage, NotUsed]]] =
    for {
      subscription <- chatDao.subscribe(user)
      serverEvents = subscription.events.map {
        case JoinedUser(user) =>
          Joined(user)
        case LeftUser(id) =>
          Disconnected(id)
        case NewMessage(message) =>
          Posted(message)
        case DeletedMessage(id) =>
          Deleted(id)
      }
      flow = Flow.fromGraph(GraphDSL.create() { implicit builder =>
        import GraphDSL.Implicits._
        val server = builder.add(Merge[ServerMessage](2))
        val client = builder.add(Flow[ClientMessage])
        serverEvents ~> server.in(0)

        client.mapAsync(1) {
          case Join =>
            val snapshot = Snapshot(
              messages = subscription.messages,
              users = subscription.users,
            )
            some(snapshot)
          case Disconnect =>
            none(_.unsubscribe(user.id))
          case Post(payload) =>
            none(_.addMessage(payload, user))
          case Delete(id) =>
            none(_.removeMessage(id, user))
        }.collect {
          case Some(response) => response
        } ~> server.in(1)

        FlowShape(client.in, server.out)
      })
    } yield Right(flow)

}