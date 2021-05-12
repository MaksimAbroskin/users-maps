package ru.dins.scalaschool.file_to_map.maps.yandex

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, HCursor, Json}
import ru.dins.scalaschool.file_to_map.Models.Note

object YaPointToMap {

  case class YaProperties(
      balloonContentHeader: String,
      balloonContentBody: String,
      balloonContentFooter: String = "<font size=1>Abroskin's edition</font>",
      clusterCaption: String = "Организация N", // as balloonContentHeader
      hintContent: String = "<strong>Всплывающая подсказка (название организации)</strong>",
  )

  object YaProperties {
    implicit val decoder: Decoder[YaProperties] =
      (c: HCursor) => {
        for {
          balloonContentHeader <- c.get[String]("balloonContentHeader")
          balloonContentBody   <- c.get[String]("balloonContentBody")
          balloonContentFooter <- c.get[String]("balloonContentFooter")
          clusterCaption       <- c.get[String]("clusterCaption")
          hintContent          <- c.get[String]("hintContent")
        } yield YaProperties(
          balloonContentHeader,
          balloonContentBody,
          balloonContentFooter,
          clusterCaption,
          hintContent,
        )
      }

    implicit val encoder: Encoder[YaProperties] = (p: YaProperties) =>
      Json.obj(
        ("balloonContentHeader", Json.fromString(p.balloonContentHeader)),
        ("balloonContentBody", Json.fromString(p.balloonContentBody)),
        ("balloonContentFooter", Json.fromString(p.balloonContentFooter)),
        ("clusterCaption", Json.fromString(p.clusterCaption)),
        ("hintContent", Json.fromString(p.hintContent)),
      )
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
      properties = YaProperties(balloonContentHeader = note.name, balloonContentBody = note.address),
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
