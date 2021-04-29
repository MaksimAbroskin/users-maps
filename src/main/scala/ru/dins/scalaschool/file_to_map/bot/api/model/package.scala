package ru.dins.scalaschool.file_to_map.bot.api

import cats.effect.Sync
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

package object model {
  implicit def jsonEncoder[A: Encoder, F[_]]: EntityEncoder[F, A]       = jsonEncoderOf[F, A]
  implicit def jsonDecoder[F[_]: Sync, A: Decoder]: EntityDecoder[F, A] = jsonOf[F, A]
}
