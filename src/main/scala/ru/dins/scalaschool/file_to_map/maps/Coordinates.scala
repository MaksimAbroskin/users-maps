package ru.dins.scalaschool.file_to_map.maps

case class Coordinates(longitude: Double, latitude: Double) {
  override def toString: String = s"$longitude $latitude"
}

object Coordinates {
  def coordinatesFromString(s: String): Coordinates = {
    val c = s.split(" ").toList.map(_.toDouble)
    Coordinates(c(1), c.head)
  }
}
