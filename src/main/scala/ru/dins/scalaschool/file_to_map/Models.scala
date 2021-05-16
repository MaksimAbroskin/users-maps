package ru.dins.scalaschool.file_to_map

import ru.dins.scalaschool.file_to_map.maps.Coordinates

object Models {

  // represent location with name, address and geographical coordinates
  case class Note(
      id: Int,         // not null
      name: String,    // not null
      address: String, // not null
      coordinates: Option[Coordinates] = None,
      info: Option[String] = None
//      phone: Option[String],     // nullable
//      typeOfHelp: Option[String],// nullable
  )

  case class InfoMessage(message: String)

  trait ErrorMessage {
    val message: String
  }

  case class SimpleError(s: String) extends ErrorMessage {
    override val message: String = s
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
      nameCol: Int = 1,
      addrCol: Int = 2,
      infoCol: Option[Int],
      lastFileId: Option[String] = None
  ) {
    val message: String =
      s"""Your current settings:
         |  $lineDelimiter - line delimiter. Info how to change it /info_set_line_del
         |  $inRowDelimiter - delimiter in row. Info how to change it /info_set_del_in_row
         |  $nameCol - number of column with name
         |  $addrCol - number of column with address
         |  ${infoCol.getOrElse("Not defined")} - number of column with info (Optional)""".stripMargin
  }

}
