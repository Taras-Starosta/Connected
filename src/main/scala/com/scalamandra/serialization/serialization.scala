package com.scalamandra

import java.time.Instant

package object serialization extends upickle.AttributeTagged {

  override implicit def OptionWriter[T: Writer]: Writer[Option[T]] =
    writer[T].comap[Option[T]] {
      case None => null.asInstanceOf[T]
      case Some(x) => x
    }

  override implicit def OptionReader[T: Reader]: Reader[Option[T]] = {
    new Reader.Delegate[Any, Option[T]](reader[T].map(Some(_))){
      override def visitNull(index: Int) = None
    }
  }

  implicit val instant: ReadWriter[Instant] =
    readwriter[String].bimap(
      time => time.toString,
      text => Instant.parse(text),
    )

}