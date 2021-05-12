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
      .map(line =>
        line.split(inLineSeparator).toList match {
          case name :: address :: Nil => Some(Note(name = name, address = address))
          case _                      => None
        },
      )
      .filter(_.isDefined)
      .map(note => note.get)
  }
}
