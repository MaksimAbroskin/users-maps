package ru.dins.scalaschool.file_to_map.maps

import fs2._
import org.http4s.client.dsl.Http4sClientDsl
import ru.dins.scalaschool.file_to_map.models.Models.Note

trait Geocoder[F[_]] {
  def getCoordinates(addr: String): F[Coordinates]

  def getCoordinatesManyPointsStream(in: Stream[F, Note]): Stream[F, F[Note]]

  def getCoordinatesManyPointsList(in: F[List[Note]]): F[List[F[Note]]]

  def getCoordinatesManyPointsList2(in: List[Note]): List[F[Note]]
}
