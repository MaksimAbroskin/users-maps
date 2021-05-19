package ru.dins.scalaschool.users_maps.maps.yandex

import cats.Traverse
import io.circe.{Decoder, HCursor, Json}

case class YaPoint(pos: String)

object YaPoint {
  implicit val decoder: Decoder[YaPoint] =
    (c: HCursor) => {
      for {
        response <- c
          .downField("response")
          .downField("GeoObjectCollection")
          .downField("featureMember")
          .as[List[Json]]
        objects <- Traverse[List]
          .traverse(response)(GeoObject =>
            GeoObject.hcursor
              .downField("GeoObject")
              .downField("Point")
              .downField("pos")
              .as[String],
          )
      } yield YaPoint(objects.head)
    }
}
