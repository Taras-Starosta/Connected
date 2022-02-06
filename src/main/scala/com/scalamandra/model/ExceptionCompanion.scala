package com.scalamandra.model

import sttp.tapir.Codec.PlainCodec
import sttp.tapir.{Codec, DecodeResult}

trait ExceptionCompanion[T <: Exception] {

  protected def codec: PlainCodec[T] = Codec.string.mapDecode(cantBeDeserialized)(_.getMessage)

  implicit def contravariantCodec[R <: T]: PlainCodec[R] = codec.asInstanceOf[PlainCodec[R]]

  case object CantBeDeserialized extends Exception("Cant be deserialized.")

  def cantBeDeserialized(serialized: String): DecodeResult.Error =
    DecodeResult.Error(
      serialized,
      CantBeDeserialized,
    )

}