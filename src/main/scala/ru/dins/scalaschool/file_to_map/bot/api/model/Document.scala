package ru.dins.scalaschool.file_to_map.bot.api.model

import io.circe.{Decoder, HCursor}

case class Document(id: String, name: Option[String])

object Document {
  implicit val userDecoder: Decoder[Document] =
    (c: HCursor) =>
      for {
        id   <- c.get[String]("file_id")
        name <- c.get[Option[String]]("file_name")
      } yield Document(id, name)
}
