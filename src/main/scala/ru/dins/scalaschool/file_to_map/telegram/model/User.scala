package ru.dins.scalaschool.file_to_map.telegram.model

import io.circe.{Decoder, HCursor}

final case class User(username: Option[String], firstName: String, lastName: Option[String])

object User {
  implicit val userDecoder: Decoder[User] =
    (c: HCursor) =>
      for {
        username <- c.get[Option[String]]("username")
        name     <- c.get[String]("first_name")
        lastName <- c.get[Option[String]]("last_name")
      } yield User(username, name, lastName)
}
