package ru.dins.scalaschool.users_maps.maps

case class Coordinates(longitude: Double, latitude: Double) {
  val asList = List(longitude, latitude)
}

object Coordinates {
  def coordinatesFromString(s: String): Coordinates = {
    val c = s.split(" ").toList.map(_.toDouble)
    Coordinates(c(1), c.head)
  }
}
