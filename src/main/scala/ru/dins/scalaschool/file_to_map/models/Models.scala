package ru.dins.scalaschool.file_to_map.models

object Models {

  case class Note(
//      id: Int,                   // not null
      name: String,              // not null
      address: String,           // not null
//      phone: Option[String],     // nullable
//      typeOfHelp: Option[String],// nullable
  ) {
    override def toString: String = s"Note = (n = $name, a = $address)"
  }

}
