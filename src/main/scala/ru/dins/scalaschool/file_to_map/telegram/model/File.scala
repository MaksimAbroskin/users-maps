package ru.dins.scalaschool.file_to_map.telegram.model

import io.circe.{Decoder, HCursor}

case class File (id: String, size: Option[Int], path:Option[String]) {
  override def toString: String = {
    s"File: " +
      s"id = $id, " +
      s"size = $size," +
      s"path = $path"
  }
}

object File {
  case class ResultHelper(id: String, size: Option[Int], path:Option[String])

  object ResultHelper {
    implicit val resultDecoder: Decoder[ResultHelper] =
      (c: HCursor) => {
        for {
          id <- c.get[String]("file_id")
          size <- c.get[Option[Int]]("file_size")
          path <- c.get[Option[String]]("file_path")
        } yield ResultHelper(id, size, path)
      }
  }

  implicit val fileDecoder: Decoder[File] =
    (c: HCursor) => {
      for {
        result <- c.get[ResultHelper]("result")
      } yield File(result.id, result.size, result.path)
    }

}
