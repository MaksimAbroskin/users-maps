package ru.dins.scalaschool.file_to_map.maps

import ru.dins.scalaschool.file_to_map.Models.{ErrorMessage, InfoMessage, Note}

trait GeocoderApi[F[_]] {
  def enrichNotes(in: List[Note]): F[Either[ErrorMessage, (List[Note], InfoMessage)]]
}
