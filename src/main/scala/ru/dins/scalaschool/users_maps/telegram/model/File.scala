package ru.dins.scalaschool.users_maps.telegram.model

import io.circe.{Decoder, HCursor}

case class File(id: String, path: Option[String])

object File {
  case class ResultHelper(id: String, path: Option[String])

  object ResultHelper {
    implicit val resultDecoder: Decoder[ResultHelper] =
      (c: HCursor) => {
        for {
          id   <- c.get[String]("file_id")
          path <- c.get[Option[String]]("file_path")
        } yield ResultHelper(id, path)
      }
  }

  implicit val fileDecoder: Decoder[File] =
    (c: HCursor) => {
      for {
        result <- c.get[ResultHelper]("result")
      } yield File(result.id, result.path)
    }
}
