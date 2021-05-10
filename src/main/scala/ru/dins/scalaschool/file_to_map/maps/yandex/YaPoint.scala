package ru.dins.scalaschool.file_to_map.maps.yandex

import cats.Traverse
import io.circe.{Decoder, HCursor, Json}

case class YaPoint(pos: String) {
  override def toString: String = s"Coordinates of your point is: $pos"
}

object YaPoint {
  implicit val decoder: Decoder[YaPoint] =
    (c: HCursor) => {
      for {
        response <- c.downField("response").downField("GeoObjectCollection").downField("featureMember").as[List[Json]]
        objects <- Traverse[List].traverse(response)(GeoObject =>
          GeoObject.hcursor.downField("GeoObject").downField("Point").downField("pos").as[String],
        )
      } yield YaPoint(objects.head)
    }
}
