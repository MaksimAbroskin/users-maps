package ru.dins.scalaschool.users_maps.maps.yandex

import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax.EncoderOps
import io.circe.{Encoder, Json}
import ru.dins.scalaschool.users_maps.Models.Note

object YaPointToMap {

  case class YaProperties(
      balloonContentHeader: String,
      balloonContentBody: String,
      balloonContentFooter: String = "<font size=1>Abroskin's edition</font>",
      clusterCaption: String,
      hintContent: String,
  )

  object YaProperties {
    implicit val encoder: Encoder[YaProperties] = deriveEncoder
  }

  case class YaGeometry(theType: String = "Point", coordinates: List[Double])

  object YaGeometry {
    implicit val encoder: Encoder[YaGeometry] = (g: YaGeometry) =>
      Json.obj(
        ("type", Json.fromString(g.theType)),
        ("coordinates", g.coordinates.asJson),
      )
  }

  case class YaOneFeature(theType: String = "Feature", id: Int, geometry: YaGeometry, properties: YaProperties)

  object YaOneFeature {
    implicit val encoder: Encoder[YaOneFeature] = (f: YaOneFeature) =>
      Json.obj(
        ("type", Json.fromString(f.theType)),
        ("id", f.id.asJson),
        ("geometry", f.geometry.asJson),
        ("properties", f.properties.asJson),
      )

    def apply(note: Note): YaOneFeature = new YaOneFeature(
      id = note.id,
      geometry = YaGeometry(coordinates = note.coordinates.get.asList),
      properties = YaProperties(
        balloonContentHeader = note.name,
        balloonContentBody = note.info match {
          case Some(info) => s"<p>Адрес: ${note.address}</p><p>Информация: $info</p>"
          case None => s"Адрес: ${note.address}"
        },
        clusterCaption = s"#${note.id}. ${note.name} ",
        hintContent = note.name,
      ),
    )

  }

  case class YaData(theType: String = "FeatureCollection", features: List[YaOneFeature])

  object YaData {
    implicit val encoder: Encoder[YaData] = (d: YaData) =>
      Json.obj(
        ("type", Json.fromString(d.theType)),
        ("features", d.features.asJson),
      )
  }

}
