package com.scalamandra

import akka.actor.typed.{ActorSystem, SpawnProtocol}
import akka.http.scaladsl.util.FastFuture
import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.scalamandra.config._
import com.scalamandra.controller._
import com.scalamandra.dao.impl._
import com.scalamandra.integration.impl._
import com.scalamandra.logging.LoggerConfigurator
import com.scalamandra.provider.impl._
import com.scalamandra.server.impl._
import com.scalamandra.service.impl._
import com.scalamandra.utils.Blocker
import doobie.hikari.HikariTransactor
import pureconfig._
import pureconfig.error.ConfigReaderException

import java.time.Clock
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.control.NonFatal

object Main {

  private lazy val source: ConfigObjectSource = ConfigSource.default

  private def loadConfig[T: ConfigReader: ClassTag](path: String): Future[T] =
    source.at(path).load match {
      case Left(failures) =>
        FastFuture.failed(new ConfigReaderException[T](failures))
      case Right(value) =>
        scribe.debug(s"Loaded configuration: $value.")
        FastFuture.successful(value)
    }

  def main(args: Array[String]): Unit = {
    implicit val ioRuntime: IORuntime = IORuntime.global
    implicit val actorSystem: ActorSystem[SpawnProtocol.Command] = ActorSystem(SpawnProtocol(), "Connected")
    implicit val ec: ExecutionContext = actorSystem.executionContext
    implicit val clock: Clock = Clock.systemDefaultZone()
    LoggerConfigurator()

    def shutdown(): Unit = {
      actorSystem.terminate()
      ioRuntime.shutdown()
    }

    val bootstrap = for {
      serverConfig <- loadConfig[ServerConfig]("server")
      apiConfig <- loadConfig[ApiConfig]("api")
      dbConf <- loadConfig[DbConfig]("db")
      tokenConfig <- loadConfig[TokenConfig]("token")
      authConfig <- loadConfig[AuthConfig]("auth")
      emailConfig <- loadConfig[EmailConfig]("email")
      bCryptConfig <- loadConfig[BCryptConfig]("bcrypt")
      chatConfig <- loadConfig[ChatConfig]("chat")
      _ <- new MigrationsImpl(dbConf).start()
      _ <- HikariTransactor.newHikariTransactor[IO](
        driverClassName = dbConf.driver,
        url = dbConf.url,
        user = dbConf.user,
        pass = dbConf.password,
        connectEC = Blocker.blockingEC,
      ).map { xa =>
        val tokenDao = new TokenDaoImpl(xa, tokenConfig)
        val userDao = new UserDaoImpl(xa)
        val mailer = new MailerImpl(emailConfig)
        val tokenProvider = new TokenProviderImpl
        val bCryptProvider = new BCryptProviderImpl(bCryptConfig)
        val authProvider = new JwtAuthProvider(authConfig)
        val authService = new AuthServiceImpl(
          mailer = mailer,
          userDao = userDao,
          tokenDao = tokenDao,
          tokenProvider = tokenProvider,
          bcryptProvider = bCryptProvider,
          authProvider = authProvider,
        )
        val authController = new AuthController(apiConfig, authService, authProvider)
        for {
          chatDao <- ChatDaoImpl.make(chatConfig, tokenProvider)
          chatService = new ChatServiceImpl(chatDao)
          chatController = new ChatController(apiConfig, authProvider, chatService)
          controllers = List(authController, chatController)
          swaggerDocs = List(authController)
          server = new ServerImpl(
            serverConfig = serverConfig,
            apiConfig = apiConfig,
            controllers = controllers,
            swaggerDocs = swaggerDocs,
          )
          binding <- server.start()
          _ = scribe.info(s"Server started.")
        } yield sys.addShutdownHook {
          scribe.info("Request server termination.")
          binding.unbind()
          shutdown()
        }
      }.useForever.unsafeToFuture()
    } yield ()

    bootstrap.recover {
      case NonFatal(exc) =>
        scribe.error(exc)
        shutdown()
    }
  }

}