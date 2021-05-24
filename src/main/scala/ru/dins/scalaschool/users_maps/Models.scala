package ru.dins.scalaschool.users_maps

import ru.dins.scalaschool.users_maps.maps.Coordinates

object Models {

  case class Note(
      id: Int,
      name: String,
      address: String,
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
      s"""Не удалось разобрать данные!
         |Пример строки с ошибкой:
         |    $failedRow
         |
         |Рекомендации:
         |  1) Сравните разделители данных с настройками бота /settings
         |  2) Сравните структуру данных с настройками бота /settings""".stripMargin
  }

  case class YaGeocoderError() extends ErrorMessage {
    override val message: String = "Не удалось определить положение точек! Проверьте корректность данных!"
  }

  case class ChatAlreadyExistsError(id: Long) extends ErrorMessage {
    override val message: String = s"Чат с id = #$id уже существует"
  }

  case class ChatNotFoundInDbError(id: Long) extends ErrorMessage {
    override val message: String = s"Чат с id = #$id не найден в базе данных"
  }

  case class UserSettings(
      chatId: Long,
      lineDelimiter: Option[Char] = None,
      inRowDelimiter: Option[Char] = None,
      nameCol: Option[Int] = None,
      addrCol: Option[Int] = None,
      infoCol: Option[Int] = None,
      city: Option[String] = None,
  ) {
    private def parseAddrCol(a: Option[Int]) = {
      val cityS = city.getOrElse("")
      a match {
        case Some(value) =>
          if (value == leftPart) s"Вся строка (адрес слева). Город: $cityS"
          else if (value == rightPart) s"Вся строка (адрес справа). Город: $cityS"
          else s"$value"
        case None => "Не определён"
      }
    }

    val message: String =
      s"""Текущие настройки:
         |Разделители:
         |  '${charAsString(lineDelimiter.getOrElse('N'))}' - разделитель между записями. Подробнее /set_line_del_desc
         |  '${charAsString(
        inRowDelimiter
          .getOrElse('N'),
      )}' - разделитель между полями записи. Подробнее /set_del_in_row_desc
         |
         |Структура данных. Подробнее /set_data_model_desc:
         |  ${nameCol.getOrElse("Не определён")} - Название
         |  ${parseAddrCol(addrCol)} - Адрес
         |  ${infoCol.getOrElse("Не определён")} - Дополнительная информация (опционально)""".stripMargin
  }

}
