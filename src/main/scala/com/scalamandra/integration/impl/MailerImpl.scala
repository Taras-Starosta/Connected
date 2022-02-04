package com.scalamandra.integration.impl

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.util.FastFuture
import com.scalamandra.config.EmailConfig
import com.scalamandra.integration.Mailer
import com.scalamandra.model.db.{Token, User}
import com.scalamandra.utils.Blocker
import courier.{Mailer => Courier, _}

import javax.mail.internet.{InternetAddress, MimeMessage}
import javax.mail.{Message, Transport}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

class MailerImpl(emailConfig: EmailConfig)
                (implicit ec: ExecutionContext,
                 val actorSystem: ActorSystem[_])
  extends Mailer with Blocker {
  import emailConfig._

  val letter: Envelope = Envelope.from(
    new InternetAddress(from)
  )

  def address(txt: String) = new InternetAddress(txt)

  object registrationLetter {

    private final val registrationSubject: Envelope =
      letter.subject("Registration submission")

    def apply(to: String, token: String): Envelope =
      registrationSubject
        .content(
          Text(s"Verification code: $token")
        ).to(
        address(to)
      )

  }

  val courier: Courier = Courier(
    host = host,
    port = port,
  ).as(
    user = emailConfig.user,
    pass = emailConfig.password,
  ).auth(true)
    .startTls(true)
    .trustAll(true)
    .debug(true)
    .ssl(true)
    .apply()

  def sendEmail(letter: Envelope): Future[Unit] = {
    def withLetterInfo(message: String): String = s"$message Destination: ${letter.to}, subject: ${letter.subject}."
    courier.send(letter).transformWith {
      case Success(_) =>
        FastFuture.successful {
          scribe.debug(withLetterInfo("Message successfully send."))
        }
      case Failure(exc) =>
        FastFuture.successful {
          scribe.error(withLetterInfo("Cannot send message."), exc)
        }
    }
  }

  override def sendConfirmation(user: User, token: Token): Future[Unit] = {
    val letter = registrationLetter(user.email, token.body)
    sendEmail(letter)
  }

  implicit class CourierOps(private val self: Courier) {

    def send(letter: Envelope): Future[Unit] = {
      val signer = self.signer
      val message = new MimeMessage(self._session) {
        letter.subject.foreach {
          case (subject, Some(charset)) => setSubject(subject, charset.name())
          case (subject, None) => setSubject(subject)
        }
        setFrom(letter.from)
        letter.to.foreach(addRecipient(Message.RecipientType.TO, _))
        letter.cc.foreach(addRecipient(Message.RecipientType.CC, _))
        letter.bcc.foreach(addRecipient(Message.RecipientType.BCC, _))
        letter.replyTo.foreach(a => setReplyTo(Array(a)))
        letter.headers.foreach(h => addHeader(h._1, h._2))
      }

      val maybeError = try {
        letter.contents match {
          case Text(txt, charset) =>
            message.setText(txt, charset.displayName)
          case mp: Multipart =>
            message.setContent(mp.parts)
          case Signed(body) =>
            if(signer.isDefined) {
              message.setContent(signer.get.sign(body))
            } else {
              new IllegalArgumentException("No signer defined, cannot sign!")
            }
        }
        Future.unit
      } catch {
        case NonFatal(exc) =>
          FastFuture.failed(exc)
      }

      val blocking = blocking {
        Transport.send(message)
      }

      maybeError.flatMap(_ => blocking)
    }

  }

}