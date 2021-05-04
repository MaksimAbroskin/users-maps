package ru.dins.scalaschool.file_to_map.bot.api.model

import io.circe.{Decoder, Encoder, HCursor, Json}

case class InputFile(filename: String, contents: Array[Byte])

object InputFile {
  implicit def inputFileEncoder: Encoder[InputFile] = Encoder.instance[InputFile] {_ => Json.Null
  }

//  implicit val inputFileDecoder: Decoder[InputFile] =
//    (c: HCursor) =>
//      for {
//        filename <- c.get[String]("filename")
//        contents     <- c.get[String]("contents")
//      } yield InputFile(filename, contents.getBytes())
}