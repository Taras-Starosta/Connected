package com.scalamandra.provider.impl

import akka.http.scaladsl.util.FastFuture
import com.scalamandra.config.AuthConfig
import com.scalamandra.model.HttpException
import com.scalamandra.model.HttpException.{InvalidJwt, Unauthorized}
import com.scalamandra.model.db.User
import com.scalamandra.model.dto.AuthedUser
import com.scalamandra.provider.AuthProvider
import com.scalamandra.provider.impl.JwtAuthProvider._
import com.scalamandra.serialization._
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtUpickle}
import sttp.tapir.EndpointOutput.OneOfVariant
import sttp.tapir._
import sttp.tapir.server.PartialServerEndpoint

import java.time.Clock
import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

class JwtAuthProvider(authConfig: AuthConfig)
                     (implicit clock: Clock) extends AuthProvider[Future, JwtClaim]{

  override def authed(errorOut: OneOfVariant[_ <: HttpException]*): PartialServerEndpoint[JwtClaim, AuthedUser, Unit, HttpException, Unit, Any, Future] =
    endpoint.securityIn(
      auth.bearer[JwtClaim]()
    ).errorOut(
      oneOf[HttpException](
        HttpException.oneOf(InvalidJwt),
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

  override def releaseAuth(user: User): String = {
    val payload = AuthedUser(
      id = user.id,
      nickname = user.nickname,
      email = user.email,
      avatarUrl = user.avatarUrl,
    )
    releaseAuth(payload)
  }

  override def releaseAuth(user: AuthedUser): String = {
    val content = write(user)
    val claim = JwtClaim(content)
    serializeClaim(claim)
  }

}
object JwtAuthProvider {

  private val invalidJwt = HttpException.error(InvalidJwt)

  private val jwtAlgorithm: JwtAlgorithm = JwtAlgorithm.HS256

  private val jwtAlgorithmSeq = Seq(JwtAlgorithm.HS256)

}