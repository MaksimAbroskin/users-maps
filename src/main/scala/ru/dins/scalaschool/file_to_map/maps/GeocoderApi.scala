package ru.dins.scalaschool.file_to_map.maps

import ru.dins.scalaschool.file_to_map.Models.{ErrorMessage, Note, NotesWithInfo}

trait GeocoderApi[F[_]] {
  def enrichNotes(in: List[Note]): F[Either[ErrorMessage, NotesWithInfo]]
}
