package ru.dins.scalaschool.file_to_map

import ru.dins.scalaschool.file_to_map.maps.Coordinates

object Models {

  // represent location with name, address and geographical coordinates
  case class Note(
      id: Int,         // not null
      name: String,    // not null
      address: String, // not null
      info: Option[String] = None,
      coordinates: Option[Coordinates] = None,
  )

  case class NotesWithInfo(
      notes: List[Note],
      info: String,
  )

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
         |  2) Compare your file data model with bot /settings""".stripMargin
  }

  case class YaGeocoderError() extends ErrorMessage {
    override val message: String = "Geocoder couldn't find your addresses! Please, check if they are correct"
  }

  case class ChatAlreadyExistsError(id: Long) extends ErrorMessage {
    override val message: String = s"Chat with id #$id already exists"
  }

  case class ChatNotFoundInDbError(id: Long) extends ErrorMessage {
    override val message: String = s"Chat with id #$id didn't found in database"
  }

  case class UserSettings(
      chatId: Long,
      lineDelimiter: Option[String] = None,
      inRowDelimiter: Option[String] = None,
      nameCol: Option[Int] = None,
      addrCol: Option[Int] = None,
      infoCol: Option[Int] = None,
  ) {
    val message: String =
      s"""Your current settings:
         |Delimiters:
         |  ${lineDelimiter.getOrElse("Not defined")} - line delimiter. Info how to change it /set_line_del_desc
         |  ${inRowDelimiter.getOrElse("Not defined")} - delimiter in row. Info how to change it /set_del_in_row_desc
         |
         |Data model (Info how to change it /set_data_model_desc):
         |  ${nameCol.getOrElse("Not defined")} - number of column with name
         |  ${addrCol.getOrElse("Not defined")} - number of column with address
         |  ${infoCol.getOrElse("Not defined")} - number of column with info (Optional)""".stripMargin
  }

}
