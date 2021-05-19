package ru.dins.scalaschool.users_maps.maps

import ru.dins.scalaschool.users_maps.Models.{ErrorMessage, Note, NotesWithInfo}

trait GeocoderApi[F[_]] {
  def enrichNotes(in: List[Note]): F[Either[ErrorMessage, NotesWithInfo]]
}
