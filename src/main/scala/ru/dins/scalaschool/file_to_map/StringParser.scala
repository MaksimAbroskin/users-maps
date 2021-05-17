package ru.dins.scalaschool.file_to_map

import Models.{ErrorMessage, FileParsingError, Note, NotesWithInfo, UserSettings}

object StringParser {
  private def parseWithErrReport(success: Int, total: Int) =
    s"File parsed.\nSuccessful: $success out of $total\nFor more details call /deepParse\n\nFetching coordinates in process. Please, wait..."
  private val parseNoErrReport = s"File parsed successful!\n\nFetching coordinates in process. Please, wait..."

  private var failedRowExample = ""

  private def parseRow(
      row: (String, Int),
      us: UserSettings,
  ): Option[Note] = {
    val pseudoNote = row._1.split(us.inRowDelimiter.getOrElse(Config.inRowDelimiter)).toList.map(_.trim)
    val name       = pseudoNote.lift(us.nameCol.getOrElse(0) - 1)
    val addr       = pseudoNote.lift(us.addrCol.getOrElse(0) - 1)
    val info = us.infoCol match {
      case Some(v) => pseudoNote.lift(v - 1)
      case None    => None
    }

    (addr, name, info) match {
      case (Some(a), Some(n), Some(i)) => Some(Note(id = row._2 + 1, n, a, None, Some(i)))
      case (Some(a), Some(n), None)    => Some(Note(id = row._2 + 1, n, a))
      case _ =>
        if (failedRowExample.isEmpty)
          failedRowExample =
            s"Line #${row._2 + 1}: ${if (row._1.length < 100) row._1 else s"${row._1.substring(0, 99)}..."}"
        None
    }
  }

  def parse(
      in: String,
      us: UserSettings,
  ): Either[ErrorMessage, NotesWithInfo] = {

    val rowsList =
      if (us.lineDelimiter.get == Config.lineDelimiter)
        in
          .split(System.lineSeparator())
          .toList
      else
        in
          .replaceAll(System.lineSeparator(), "")
          .split(us.lineDelimiter.get)
          .toList

    val notes = rowsList.zipWithIndex
      .map(row => parseRow(row, us))

    val result = notes.flatten
    if (result.isEmpty) Left(FileParsingError(failedRowExample))
    else {
      Right(
        NotesWithInfo(
          result,
          if (result.length != notes.length)
            parseWithErrReport(result.length, notes.length)
          else parseNoErrReport,
        ),
      )
    }
  }

  //TODO deepParse() with info about parsing errors

}
