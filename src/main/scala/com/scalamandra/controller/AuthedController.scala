package com.scalamandra.controller

import akka.http.scaladsl.util.FastFuture
import com.scalamandra.config.AuthConfig
import com.scalamandra.controller.AuthedController.invalidJwt
import com.scalamandra.model.HttpException._
import com.scalamandra.model.dto.AuthedUser
import com.scalamandra.serialization._
import pdi.jwt._
import sttp.tapir._

import java.time.Clock
import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

trait AuthedController extends Controller {

  implicit def clock: Clock

  def authConfig: AuthConfig

  protected val authed =
    endpoint.securityIn(
      auth.bearer[JwtClaim]()
    ).errorOut(
      oneOf[Unauthorized](
        oneOfHttp(InvalidJwt)
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

  private val jwtAlgorithm: JwtAlgorithm = JwtAlgorithm.HS256

  private val jwtAlgorithmSeq = Seq(JwtAlgorithm.HS256)

  implicit val jwtClaimCodec: Codec[List[String], JwtClaim, CodecFormat.TextPlain] =
    implicitly[Codec[List[String], String, CodecFormat.TextPlain]]
      .mapDecode { text =>
        JwtUpickle.decode(text, authConfig.appSecret, jwtAlgorithmSeq) match {
          case Success(value) => DecodeResult.Value(value)
          case Failure(exc) => DecodeResult.Error(text, exc)
        }
      } { claim =>
        val configured = claim.issuedNow.expiresIn(authConfig.jwtTtl.toSeconds)
        JwtUpickle.encode(configured, authConfig.appSecret, jwtAlgorithm)
      }

}
object AuthedController {

  private def failedAuth(reason: Unauthorized): Future[Either[Unauthorized, AuthedUser]] =
    FastFuture.successful(Left(reason))

  private val invalidJwt = failedAuth(InvalidJwt)

}