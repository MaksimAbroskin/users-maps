package ru.dins.scalaschool.file_to_map.bot.api.model

import cats.Traverse
import io.circe.{Decoder, HCursor, Json}

case class YandexPoint(pos: String) {
  override def toString: String = s"Coordinates of your point is: $pos"
}

object YandexPoint {
  implicit val decoder: Decoder[YandexPoint] =
    (c: HCursor) => {
      for {
        response <- c.downField("response").downField("GeoObjectCollection").downField("featureMember").as[List[Json]]
        objects <- Traverse[List].traverse(response)(GeoObject =>
          GeoObject.hcursor.downField("GeoObject").downField("Point").downField("pos").as[String],
        )
      } yield YandexPoint(objects.head)
    }
}
