package ru.dins.scalaschool.file_to_map.maps

trait Geocoder[F[_]] {
  def getCoordinates(addr: String): F[Coordinates]
}
