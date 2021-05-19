package ru.dins.scalaschool.users_maps.telegram.model

import io.circe.{Decoder, HCursor}

case class Document(id: String)

object Document {
  implicit val userDecoder: Decoder[Document] =
    (c: HCursor) =>
      for {
        id <- c.get[String]("file_id")
      } yield Document(id)
}
