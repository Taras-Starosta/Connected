package com.scalamandra.dao.impl

import akka.NotUsed
import akka.actor.typed.SpawnProtocol.Spawn
import akka.actor.typed._
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.OverflowStrategy
import akka.stream.scaladsl._
import com.scalamandra.config.ChatConfig
import com.scalamandra.dao.ChatDao
import com.scalamandra.dao.ChatDao.ChatSubscription
import com.scalamandra.dao.impl.ChatDaoImpl._
import com.scalamandra.model.db.ChatEvent._
import com.scalamandra.model.db.{ChatEvent, Message}
import com.scalamandra.model.dto.auth.AuthedUser
import com.scalamandra.provider.TokenProvider

import java.time.{Clock, Instant}
import scala.concurrent.{ExecutionContext, Future}

class ChatDaoImpl(actor: ActorRef[ChatCommand],
                  config: ChatConfig,
                  tokenProvider: TokenProvider)
                 (implicit
                  actorSystem: ActorSystem[SpawnProtocol.Command],
                  ec: ExecutionContext,
                  clock: Clock) extends ChatDao {
  import config._

  override def addMessage(payload: String, author: AuthedUser): Future[Unit] = {
    val message = Message(
      id = tokenProvider.generateToken,
      payload = payload,
      author = author.id,
      timestamp = Instant.now(clock)
    )
    actor ! AddMessage(message)
    Future.unit
  }

  override def removeMessage(id: String, user: AuthedUser): Future[Unit] = {
    actor ! RemoveMessage(id, user)
    Future.unit
  }

  override def subscribe(user: AuthedUser): Future[ChatSubscription] = {
    val (eventQ, eventS) = Chatroom.makeQueue
    for {
      snapshot <- actor.ask[Snapshot] { replyTo =>
        Subscribe(
          user = user,
          eventQueue = eventQ,
          replyTo = replyTo,
        )
      }
      (messages, users) = snapshot
    } yield ChatSubscription(
      messages = messages,
      users = users,
      events = eventS,
    )
  }

  override def unsubscribe(id: Long): Future[Unit] = {
    actor ! Unsubscribe(id)
    Future.unit
  }

}
object ChatDaoImpl {

  type Snapshot = (Iterable[Message], Iterable[AuthedUser])
  type EventQueue = SourceQueueWithComplete[ChatEvent]

  sealed trait ChatCommand
  case class AddMessage(message: Message) extends ChatCommand
  case class RemoveMessage(id: String, user: AuthedUser) extends ChatCommand
  case class Subscribe(
                        user: AuthedUser,
                        eventQueue: EventQueue,
                        replyTo: ActorRef[Snapshot],
                      ) extends ChatCommand
  case class Unsubscribe(id: Long) extends ChatCommand

  object Chatroom {

    case class Subscription(
                             user: AuthedUser,
                             eventQueue: EventQueue,
                           ) {

      def publish(event: ChatEvent): Unit =
        eventQueue.offer(event)

      def unsubscribe(): Unit =
        eventQueue.complete()

    }

    def makeQueue(implicit actorSystem: ActorSystem[_]): (EventQueue, Source[ChatEvent, NotUsed]) =
      Source.queue[ChatEvent](
        8,
        OverflowStrategy.dropTail,
        2,
      ).toMat(
        BroadcastHub.sink(8)
      )(Keep.both).run()

    def act(subscriptions: Map[Long, Subscription],
            messages: Map[String, Message]): Behavior[ChatCommand] = {

      def publish(event: ChatEvent): Unit = for {
        subscription <- subscriptions.values
      } subscription.publish(event)

      Behaviors.receiveMessage {
        case AddMessage(message) =>
          if(!messages.contains(message.id)) {
            publish(NewMessage(message))
            act(
              subscriptions,
              messages.updated(message.id, message),
            )
          } else Behaviors.same

        case RemoveMessage(id, user) =>
          val isValid = messages.get(id).exists(_.author == user.id)
          if(isValid) {
            publish(DeletedMessage(id))
            act(
              subscriptions,
              messages.removed(id),
            )
          } else Behaviors.same

        case Subscribe(user, eventQ, replyTo) =>
          if(!subscriptions.contains(user.id)) {
            val subscription = Subscription(user, eventQ)
            val messagesSnapshot = messages.values
            val usersSnapshot = subscriptions.values.map(_.user)
            replyTo ! (messagesSnapshot, usersSnapshot)
            publish(JoinedUser(user))
            act(
              subscriptions.updated(user.id, subscription),
              messages,
            )
          } else Behaviors.same

        case Unsubscribe(id) =>
          if(subscriptions.contains(id)) {
            subscriptions(id).unsubscribe()
            publish(LeftUser(id))
            act(
              subscriptions.removed(id),
              messages,
            )
          } else Behaviors.same
      }
    }

  }

  def make(config: ChatConfig,
           tokenProvider: TokenProvider)
          (implicit actorSystem: ActorSystem[SpawnProtocol.Command],
           ec: ExecutionContext,
           clock: Clock): Future[ChatDao] = {
    import config._
    for {
      ref <- actorSystem.ask[ActorRef[ChatCommand]] { replyTo =>
        Spawn(
          behavior = Chatroom.act(
            Map.empty,
            Map.empty,
          ),
          name = "Chatroom",
          props = Props.empty,
          replyTo = replyTo,
        )
      }
    } yield new ChatDaoImpl(
      actor = ref,
      config = config,
      tokenProvider = tokenProvider,
    )
  }

}