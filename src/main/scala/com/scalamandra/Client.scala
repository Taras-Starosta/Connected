package com.scalamandra

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.ws._
import akka.stream.scaladsl._
import com.scalamandra.serialization._
import com.scalamandra.model.dto.chat.ChatProtocol._

import scala.concurrent.Future

object Client {
  def main(args: Array[String]): Unit = {
    val key = args(0)
    implicit val system: ActorSystem = ActorSystem()
    import system.dispatcher

    val commands = for {
      command <- Vector(Join, Post("Hello"), Disconnect)
      json = write[ClientMessage](command)
      message = TextMessage(json)
    } yield message

    val outgoing = Source(commands)

    val incoming: Sink[Message, Future[Done]] =
      Sink.foreach[Message] {
        case message: TextMessage.Strict =>
          println(message.text)
        case _ =>
      }

    val webSocketFlow = Http().webSocketClientFlow(
      WebSocketRequest(
        s"ws://localhost:9999/api/v1/chat?key=$key",
        Vector(RawHeader("X-Forwarded-For", "127.0.0.1:42246")),
      )
    )

    val (upgradeResponse, closed) =
    outgoing
      .viaMat(webSocketFlow)(Keep.right)
      .toMat(incoming)(Keep.both)
      .run()

    val connected = upgradeResponse.flatMap { upgrade =>
      if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
        Future.successful(Done)
      } else {
        throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
      }
    }

    connected.onComplete(println)
    closed.foreach(_ => println("closed"))
  }
}