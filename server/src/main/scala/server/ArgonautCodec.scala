package server

import argonaut.{DecodeJson, EncodeJson}
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.argonaut.{jsonEncoderOf, jsonOf}


trait ArgonautCodec {
  implicit def decoder[A: DecodeJson]: EntityDecoder[A] = jsonOf[A]
  implicit def encoder[A: EncodeJson]: EntityEncoder[A] = jsonEncoderOf[A]
}

object ArgonautCodec extends ArgonautCodec
