package ru.dins.scalaschool.file_to_map

import Models.{ErrorMessage, FileParsingError, Note}

object FileParser {
  def parse(in: String): Either[ErrorMessage, List[Note]] = {

    var failedRowExample = ""

    val oneParsedList = in
      .replaceAll(System.lineSeparator(), "")
      .split(Config.lineDelimiter)
      .toList
      .zipWithIndex
      .map(line =>
        line._1.split(Config.inRowDelimiter).toList match {
          case name :: address :: Nil => Some(Note(id = line._2, name = name, address = address))
          case _ =>
            failedRowExample = line._1
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
}
