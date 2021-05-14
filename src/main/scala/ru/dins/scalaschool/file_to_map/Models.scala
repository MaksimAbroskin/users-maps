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

  trait ErrorMessage {
    val message: String
  }

  case class FileParsingError(failedRow: String) extends ErrorMessage {
    override val message: String =
      s"""File parsing error!
         |Too much rows didn't parsed. Example of failed row:
         |    $failedRow
         |
         |Recommendations:
         |  1) Compare your document delimiters with bot /settings
         |  2) Check amount and content of fields in your document""".stripMargin
  }

  case class YaGeocoderError(addr: String) extends ErrorMessage {
    override val message: String = s"Couldn't take coordinates for address: $addr"
  }

}
