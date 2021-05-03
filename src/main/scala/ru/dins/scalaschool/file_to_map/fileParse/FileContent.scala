package ru.dins.scalaschool.file_to_map.fileParse

import ru.dins.scalaschool.file_to_map.models.Models.Note

case class FileContent(content: String) {

  def stringToNotesList(s: String, lineSeparator: String, inLineSeparator: String): List[Note] = {
    val strNotes = s.split(lineSeparator).toList.map { s =>
      val sNote = s.split(inLineSeparator).toList
      sNote match {
        case name :: addr :: Nil => Some(Note(name, addr))
        case _                   => None
      }
    }
    strNotes.filterNot(_.isEmpty).map(x => x.get)
  }

}

object FileContent {}
