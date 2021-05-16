package ru.dins.scalaschool.file_to_map

import Models.{ErrorMessage, FileParsingError, InfoMessage, Note}

object FileParser {
  private def parseWithErrReport(success: Int, total: Int) =
    s"File parsed.\nSuccessful: $success out of $total\nFor more details call /deepParse\n\nFetching coordinates in process. Please, wait..."
  private val parseNoErrReport = s"File parsed successful!\n\nFetching coordinates in process. Please, wait..."

  private def parseRow(
      row: (String, Int),
      inRowDelimiter: String,
      nameCol: Int,
      addrCol: Int,
      infoCol: Option[Int] = None,
  ): Option[Note] = {
    val pseudoNote = row._1.split(inRowDelimiter).toList.map(_.trim)
    val name       = pseudoNote.lift(nameCol - 1)
    val addr       = pseudoNote.lift(addrCol - 1)
    val info = infoCol match {
      case Some(v) => pseudoNote.lift(v - 1)
      case None    => None
    }
    println(s"name = $name")
    println(s"addr = $addr")
    println(s"info = $info")
    (addr, name, info) match {
      case (Some(a), Some(n), Some(i)) => Some(Note(id = row._2 + 1, n, a, None, Some(i)))
      case (Some(a), Some(n), None)    => Some(Note(id = row._2 + 1, n, a))
      case _                           => None
    }
  }

  def parse(
      in: String,
      lineDelimiter: String,
      inRowDelimiter: String,
      nameCol: Int,
      addrCol: Int,
      infoCol: Option[Int] = None,
  ): Either[ErrorMessage, (List[Note], InfoMessage)] = {

    var failedRowExample = ""

    val listParsedByLines =
      if (lineDelimiter != Config.newLine)
        in
          .replaceAll(System.lineSeparator(), "")
          .split(lineDelimiter)
          .toList
      else
        in
          .split(System.lineSeparator())
          .toList

    //TODO ability to set order of columns with data: address, name, additional info for display on map
    val notes = listParsedByLines.zipWithIndex
      .map(row => parseRow(row, inRowDelimiter, nameCol, addrCol, infoCol))
//      .map(line =>
//        line._1
//          .split(inRowDelimiter)
//          .toList
//          .map(_.trim) match {
//          case name :: address :: Nil => Some(Note(id = line._2 + 1, name = name, address = address))
//          case _ =>
//            if (failedRowExample.isEmpty) {
//              failedRowExample =
//                s"Line #${line._2 + 1}: ${if (line._1.length < 100) line._1 else s"${line._1.substring(0, 99)}..."}"
//            }
//            None
//        },
//      )

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
