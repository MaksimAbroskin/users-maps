package ru.dins.scalaschool.file_to_map

import Models.{ErrorMessage, FileParsingError, Note}

object FileParser {
  def parse(in: String, lineDelimiter: String, inRowDelimiter: String): Either[ErrorMessage, List[Note]] = {

    var failedRowExample = ""

    val oneParsedList = in
      .replaceAll(System.lineSeparator(), "")
      .split(lineDelimiter)
      .toList
      .zipWithIndex
      .map(line =>
        line._1
          .split(inRowDelimiter)
          .toList
          .map(_.trim) match {
          case name :: address :: Nil => Some(Note(id = line._2 + 1, name = name, address = address))
          case _ =>
            failedRowExample =
              s"Line #${line._2 + 1}: ${if (line._1.length < 50) line._1 else line._1.substring(0, 49)}"
            None
        },
      )

    val oplSize     = oneParsedList.length
    val noneCounter = oneParsedList.count(_.isEmpty)
    if (noneCounter > 10 || noneCounter > oplSize * 0.1) Left(FileParsingError(failedRowExample))
    else
      Right(
        oneParsedList
          .filter(_.isDefined)
          .map(note => note.get),
      )
  }

  def parseWithErrInfo(in: String, lineDelimiter: String, inRowDelimiter: String): ErrorMessage = {
    val oneParsedList = in
      .replaceAll(System.lineSeparator(), "")
      .split(lineDelimiter)
      .toList
      .zipWithIndex
      .map(line =>
        line._1
          .split(inRowDelimiter)
          .toList
          .map(_.trim) match {
          case _ :: _ :: Nil => None
          case _ => Some(line._2 + 1)
        },
      )
    val failedList = oneParsedList.filter(_.isDefined).map(_.get)
    if (failedList.isEmpty) FileParsingError("All notes parsed correctly!")
    else FileParsingError(s"Numbers of failed rows:\n${failedList.mkString(", ")}")
  }

}
