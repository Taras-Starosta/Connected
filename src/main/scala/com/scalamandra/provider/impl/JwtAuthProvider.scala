package com.scalamandra.provider.impl

import akka.http.scaladsl.util.FastFuture
import com.scalamandra.config.AuthConfig
import com.scalamandra.dao.ApiKeyDao
import com.scalamandra.model.HttpException.InvalidJwt
import com.scalamandra.model.WsException.InvalidApiKey
import com.scalamandra.model.db.User
import com.scalamandra.model.dto.auth.AuthedUser
import com.scalamandra.model.{HttpException, WsException}
import com.scalamandra.provider.AuthProvider
import com.scalamandra.provider.impl.JwtAuthProvider._
import com.scalamandra.serialization._
import com.scalamandra.utils.ExceptionUtils
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtUpickle}
import sttp.tapir.EndpointOutput.OneOfVariant
import sttp.tapir._
import sttp.tapir.server.PartialServerEndpoint

import java.time.Clock
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

class JwtAuthProvider(authConfig: AuthConfig,
                      apiKeyDao: ApiKeyDao)
                     (implicit clock: Clock,
                      ec: ExecutionContext) extends AuthProvider[Future, JwtClaim] {

  override def httpAuthed(errorOut: OneOfVariant[_ <: HttpException]*): PartialServerEndpoint[JwtClaim, AuthedUser, Unit, HttpException, Unit, Any, Future] =
    endpoint.securityIn(
      auth.bearer[JwtClaim]()
    ).errorOut(
      oneOf[HttpException](
        ExceptionUtils.oneOfHttp(InvalidJwt),
        errorOut: _*,
      )
    ).serverSecurityLogic[AuthedUser, Future] { jwt =>
      if(jwt.isValid) {
        try {
          val user = read[AuthedUser](jwt.content)
          FastFuture.successful(Right(user))
        } catch {
          case NonFatal(_) =>
            invalidJwt
        }
      } else {
        invalidJwt
      }
    }

  override def wsAuthed(errorOut: OneOfVariant[_ <: WsException]*): PartialServerEndpoint[KeyAndIp, AuthedUser, Unit, WsException, Unit, Any, Future] =
    endpoint.securityIn(
      auth.apiKey(query[String]("key"))
        .and(clientIp)
    ).errorOut(
      oneOf[WsException](
        ExceptionUtils.oneOfWs(InvalidApiKey),
        errorOut: _*,
      )
    ).serverSecurityLogic[AuthedUser, Future] { case (apiKey, maybeIp) =>
      maybeIp match {
        case Some(ip) =>
          for {
            maybeUser <- apiKeyDao.validate(apiKey, ip)
            result = maybeUser match {
              case Some(value) => Right(value)
              case None => Left(InvalidApiKey)
            }
          } yield result
        case None =>
          ExceptionUtils.error(InvalidApiKey)
      }
    }

  implicit val jwtClaimCodec: Codec[List[String], JwtClaim, CodecFormat.TextPlain] =
    implicitly[Codec[List[String], String, CodecFormat.TextPlain]]
      .mapDecode { text =>
        JwtUpickle.decode(text, authConfig.appSecret, jwtAlgorithmSeq) match {
          case Success(value) => DecodeResult.Value(value)
          case Failure(exc) => DecodeResult.Error(text, exc)
        }
      }(serializeClaim)

  def serializeClaim(claim: JwtClaim): String = {
    val configured = claim.issuedNow.expiresIn(authConfig.jwtTtl.toSeconds)
    JwtUpickle.encode(configured, authConfig.appSecret, jwtAlgorithm)
  }

  def authed(user: User): AuthedUser = AuthedUser(
    id = user.id,
    nickname = user.nickname,
    email = user.email,
    avatarUrl = user.avatarUrl,
  )

  override def releaseJwt(user: User): Future[String] =
    releaseJwt(authed(user))

  override def releaseJwt(user: AuthedUser): Future[String] = Future {
    val content = write(user)
    val claim = JwtClaim(content)
    serializeClaim(claim)
  }

  override def releaseApiKey(user: User, ip: String): Future[String] =
    releaseApiKey(authed(user), ip)

  override def releaseApiKey(user: AuthedUser, ip: String): Future[String] =
    apiKeyDao.release(user, ip)

}
object JwtAuthProvider {

  private val invalidJwt = ExceptionUtils.error(InvalidJwt)

  private val jwtAlgorithm: JwtAlgorithm = JwtAlgorithm.HS256

  private val jwtAlgorithmSeq = Seq(JwtAlgorithm.HS256)

}