package ru.dins.scalaschool.users_maps

import Models.{ErrorMessage, FileParsingError, Note, NotesWithInfo, UserSettings}

object StringParser {
  def parseWithErrReport(success: Int, total: Int, example: String): String =
    s"""Data parsed.
       |Successfully: $success out of $total
       |
       |Example of failed row:
       |    $example
       |
       |Recommendations:
       |  1) Compare your document delimiters with bot /settings
       |  2) Compare your file data model with bot /settings
       |
       |
       |  Fetching coordinates in process. Please, wait...""".stripMargin
  def parseNoErrReport(success: Int, total: Int) =
    s"Data parsed successfully ($success out of $total)!\n\nFetching coordinates in process. Please, wait..."

  private def parseRow(
      row: (String, Int),
      us: UserSettings,
  ): (Option[Note], Option[String]) = {
    val pseudoNote = row._1.split(us.inRowDelimiter.getOrElse(defaultInRowDelimiter)).toList.map(_.trim)
    val name       = pseudoNote.lift(us.nameCol.getOrElse(defaultNameCol) - 1)
    val addr       = pseudoNote.lift(us.addrCol.getOrElse(defaultAddrCol) - 1)
    val info = us.infoCol match {
      case Some(v) => pseudoNote.lift(v - 1)
      case None    => None
    }

    (addr, name, info) match {
      case (Some(a), Some(n), Some(i)) => (Some(Note(id = row._2 + 1, n, a, Some(i))), None)
      case (Some(a), Some(n), None)    => (Some(Note(id = row._2 + 1, n, a)), None)
      case _ =>
        (None, Some(s"Line #${row._2 + 1}: ${if (row._1.length < 100) row._1 else s"${row._1.substring(0, 99)}..."}"))
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

    val result     = notes.flatMap(_._1)
    val errExample = notes.flatMap(_._2).headOption.getOrElse("")

    if (result.isEmpty) Left(FileParsingError(errExample))
    else {
      Right(
        NotesWithInfo(
          result,
          if (result.length != notes.length)
            parseWithErrReport(result.length, notes.length, errExample)
          else parseNoErrReport(result.length, notes.length),
        ),
      )
    }
  }
}
