package ru.dins.scalaschool.file_to_map

import ru.dins.scalaschool.file_to_map.maps.Coordinates

object Models {

  // represent location with name, address and geographical coordinates
  case class Note(
      id: Int,         // not null
      name: String,    // not null
      address: String, // not null
      coordinates: Option[Coordinates] = None,
//      phone: Option[String],     // nullable
//      typeOfHelp: Option[String],// nullable
  )

  case class InfoMessage(message: String)

  trait ErrorMessage {
    val message: String
  }

  case class FileParsingError(failedRow: String) extends ErrorMessage {
    override val message: String =
      s"""File didn't parsed!
         |Example of failed row:
         |    $failedRow
         |
         |Recommendations:
         |  1) Compare your document delimiters with bot /settings
         |  2) Check amount and content of columns in your document""".stripMargin
  }

  case class YaGeocoderError() extends ErrorMessage {
    override val message: String = "Yandex geocoder API is not available. Please, try later"
  }

  case class DatabaseError(s: String) extends ErrorMessage {
    override val message: String = s"Error while working with the database: $s"
  }

  case class UserSettings(
      chatId: String,
      lineDelimiter: String = "'",
      inRowDelimiter: String = ":",
      lastFileId: Option[String] = None
  )

}
