package ru.dins.scalaschool.file_to_map

import Models.Note

object FileParser {
  def parse(in: String): List[Note] = {

    val lineSeparator   = "'"
    val inLineSeparator = ":"
    in
      .replaceAll(System.lineSeparator(), "")
      .split(lineSeparator)
      .toList
      .zipWithIndex
      .map(line =>
        line._1.split(inLineSeparator).toList match {
          case name :: address :: Nil => Some(Note(id = line._2, name = name, address = address))
          case _                      => None
        },
      )
      .filter(_.isDefined)
      .map(note => note.get)
  }
}
