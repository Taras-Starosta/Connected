package com.scalamandra.controller
import com.scalamandra.config.ApiConfig
import com.scalamandra.model.dto.chat.ChatProtocol.{ClientMessage, ServerMessage}
import com.scalamandra.provider.AuthProvider
import com.scalamandra.service.ChatService
import pdi.jwt.JwtClaim
import sttp.capabilities.akka.AkkaStreams
import sttp.tapir._
import sttp.tapir.generic.auto._

import scala.concurrent.Future

class ChatController(
                      val apiConfig: ApiConfig,
                      authProvider: AuthProvider[Future, JwtClaim],
                      chatService: ChatService,
                    ) extends Controller {

  override lazy val endpoints: List[Endpoint] = List(chat)

  def chat: Endpoint =
    authProvider.wsAuthed()
      .description("Chat websockets endpoint.")
      .in(version / "chat")
      .out(
        webSocketBody[ClientMessage, CodecFormat.Json, ServerMessage, CodecFormat.Json](AkkaStreams)
      )
      .serverLogic { u => _ =>
        chatService.handle(u)
      }

}