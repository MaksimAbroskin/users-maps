package ru.dins.scalaschool.file_to_map

import Models.{ErrorMessage, FileParsingError, InfoMessage, Note}

object FileParser {
  private def parseWithErrReport(success: Int, total: Int) =
    s"File parsed.\nSuccessful: $success out of $total\nFor more details call /deepParse\n\nFetching coordinates in process. Please, wait..."
  private val parseNoErrReport = s"File parsed successful!\n\nFetching coordinates in process. Please, wait..."

  def parse(
      in: String,
      lineDelimiter: String,
      inRowDelimiter: String,
  ): Either[ErrorMessage, (List[Note], InfoMessage)] = {

    var failedRowExample = ""

    val listParsedByLines = in
      .replaceAll(System.lineSeparator(), "")
      .split(lineDelimiter)
      .toList

    val notes = listParsedByLines.zipWithIndex
      .map(line =>
        line._1
          .split(inRowDelimiter)
          .toList
          .map(_.trim) match {
          case name :: address :: Nil => Some(Note(id = line._2 + 1, name = name, address = address))
          case _ =>
            failedRowExample =
              s"Line #${line._2 + 1}: ${if (line._1.length < 100) line._1 else s"${line._1.substring(0, 99)}..."}"
            None
        },
      )

    val result = notes.flatten
    if (result.isEmpty) Left(FileParsingError(failedRowExample))
    else {
      Right(
        result,
        InfoMessage(
          if (result.length != listParsedByLines.length)
            parseWithErrReport(result.length, listParsedByLines.length)
          else parseNoErrReport,
        ),
      )
    }
  }

  //TODO deepParse() with info about parsing errors

//  def parseWithErrInfo(in: String, lineDelimiter: String, inRowDelimiter: String): ErrorMessage = {
//    val oneParsedList = in
//      .replaceAll(System.lineSeparator(), "")
//      .split(lineDelimiter)
//      .toList
//      .zipWithIndex
//      .map(line =>
//        line._1
//          .split(inRowDelimiter)
//          .toList
//          .map(_.trim) match {
//          case _ :: _ :: Nil => None
//          case _             => Some(line._2 + 1)
//        },
//      )
//    val failedList = oneParsedList.flatten
//    if (failedList.isEmpty) FileParsingError("All notes parsed correctly!")
//    else FileParsingError(s"Numbers of failed rows:\n${failedList.mkString(", ")}")
//  }

}
