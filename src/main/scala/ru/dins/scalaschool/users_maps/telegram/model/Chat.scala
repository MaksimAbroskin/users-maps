package ru.dins.scalaschool.users_maps.telegram.model

import io.circe.Decoder
import io.circe.generic.semiauto._

final case class Chat(id: Long)

object Chat {
  implicit val chatDecoder: Decoder[Chat] = deriveDecoder
}
