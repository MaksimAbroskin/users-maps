package ru.dins.scalaschool.users_maps

import Models.{ErrorMessage, FileParsingError, Note, NotesWithInfo, UserSettings}

object StringParser {
  def parseWithErrReport(success: Int, total: Int, example: String): String =
    s"""Данные разобраны.
       |Успешно: $success из $total
       |
       |Пример строки с ошибкой:
       |    $example
       |
       |Рекомендации:
       |  1) Сравните разделители данных с настройками бота /settings
       |  2) Сравните структуру данных с настройками бота /settings
       |
       |
       |  Идёт получение координат. Пожалуйста, подождите...""".stripMargin
  def parseNoErrReport(success: Int, total: Int) =
    s"Данные успешно разобраны ($success из $total)!\n\nИдёт получение координат. Пожалуйста, подождите..."

  private def parseRow(
      row: (String, Int),
      us: UserSettings,
  ): Either[String, Note] = {
    val s     = row._1
    val index = row._2 + 1

    if (us.addrCol.isDefined & us.nameCol.isEmpty & us.infoCol.isEmpty) {
      val direction = us.addrCol.getOrElse(99)
      val clippedAddr =
        if ((direction == leftPart) && (s.length > maxAddrLength)) s.substring(0, maxAddrLength)
        else if ((direction == rightPart) && (s.length > maxAddrLength)) s.substring(s.length - maxAddrLength)
        else s

      val extendedAddr = s"""${us.city.getOrElse("")} $clippedAddr"""

      if (direction == leftPart) Right(Note(id = index, s, extendedAddr))
      else if (direction == rightPart) Right(Note(id = index, s, extendedAddr))
      else
        Left(s"Строка #$index: ${if (s.length < 100) s else s"${s.substring(0, 99)}..."}")
    } else {

      val pseudoNote = s.split(us.inRowDelimiter.getOrElse(defaultInRowDelimiter)).toList.map(_.trim)
      val name       = pseudoNote.lift(us.nameCol.getOrElse(defaultNameCol) - 1)
      val addr       = pseudoNote.lift(us.addrCol.getOrElse(defaultAddrCol) - 1)
      val info = us.infoCol match {
        case Some(v) => pseudoNote.lift(v - 1)
        case None    => None
      }

      (addr, name, info) match {
        case (Some(a), Some(n), Some(i)) => Right(Note(id = index, n, a, Some(i)))
        case (Some(a), Some(n), None)    => Right(Note(id = index, n, a))
        case _                           => Left(s"Строка #$index: ${if (s.length < 100) row._1 else s"${s.substring(0, 99)}..."}")
      }
    }
  }

  def parse(
      in: String,
      us: UserSettings,
  ): Either[ErrorMessage, NotesWithInfo] = {

    val lineSeparator = "\n|\r\n|\r"

    val rows =
      if (us.lineDelimiter.getOrElse(defaultLineDelimiter) == newLine)
        in
          .split(lineSeparator)
      else
        in
          .replaceAll(lineSeparator, "")
          .split(us.lineDelimiter.getOrElse(defaultLineDelimiter))

    val notes = rows.toList.zipWithIndex
      .map(row => parseRow(row, us))

    val result = notes.flatMap(x => x.toOption)
    val tooMuch =
      if (result.length > oneMessageLimit)
        s"Данные содержат более $oneMessageLimit записей!\nОбработаны будут только первые $oneMessageLimit"
      else ""
    val errExample = notes.find(_.isLeft).flatMap(_.swap.toOption).getOrElse("")

    val limitedResult = result.take(oneMessageLimit)

    if (limitedResult.isEmpty) Left(FileParsingError(errExample))
    else {
      Right(
        NotesWithInfo(
          limitedResult,
          if (tooMuch.nonEmpty) tooMuch
          else if (limitedResult.length != notes.length)
            parseWithErrReport(limitedResult.length, notes.length, errExample)
          else parseNoErrReport(limitedResult.length, notes.length),
        ),
      )
    }
  }
}
