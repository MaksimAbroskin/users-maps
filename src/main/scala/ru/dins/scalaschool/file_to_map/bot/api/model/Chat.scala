package ru.dins.scalaschool.file_to_map.bot.api.model

import io.circe.{Decoder, HCursor}

final case class Chat(id: Long)

object Chat {
  implicit val chatDecoder: Decoder[Chat] = Decoder.instance((c: HCursor) => c.get[Long]("id").map(Chat.apply))
}
