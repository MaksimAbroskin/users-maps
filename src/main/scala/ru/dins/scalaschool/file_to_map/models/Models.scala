package ru.dins.scalaschool.file_to_map.models

import ru.dins.scalaschool.file_to_map.maps.Coordinates

object Models {

  case class Note(
//      id: Int,                   // not null
      name: String,              // not null
      address: String,           // not null
      coordinates: Option[Coordinates] = None
//      phone: Option[String],     // nullable
//      typeOfHelp: Option[String],// nullable
  ) {
    override def toString: String = s"Note = (n = $name, a = $address, coord = $coordinates)"
  }

}
