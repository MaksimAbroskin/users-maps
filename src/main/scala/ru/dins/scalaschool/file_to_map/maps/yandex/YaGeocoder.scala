package ru.dins.scalaschool.file_to_map.maps.yandex

import cats.effect.Sync
import cats.syntax.functor._
import org.http4s._
import org.http4s.client.Client
import org.http4s.implicits._
import ru.dins.scalaschool.file_to_map.telegram.model.jsonDecoder
import ru.dins.scalaschool.file_to_map.maps.{Coordinates, GeocoderApi}
import ru.dins.scalaschool.file_to_map.Models._

trait YaGeocoder[F[_]] extends GeocoderApi[F]

object YaGeocoder {
  def apply[F[_]: Sync](client: Client[F]): YaGeocoder[F] =
    new YaGeocoder[F] {

      val geocoderUri: Uri = uri"""https://geocode-maps.yandex.ru/1.x"""
      val yandexApiKey = "85e83a9b-10f8-4dd2-98db-47687cb13067"
      //      https://geocode-maps.yandex.ru/1.x?geocode=<addr>&apikey=<yandexApiKey>&format=json&results=1
      private def getCoordinatesUri(addr: String): Uri = geocoderUri =? Map(
        "geocode" -> List(addr),
        "apikey" -> List(yandexApiKey),
        "format" -> List("json"),
        "results" -> List("1"),
      )

      private def getCoordinates(addr: String): F[Coordinates] =
        client
          .expect[YaPoint](
            Request[F](uri = getCoordinatesUri(addr), method = Method.GET))
          .map(point => Coordinates.coordinatesFromString(point.pos))

      override def enrichNotes(in: List[Note]): F[List[Note]] = {
        import cats.implicits._
        in.traverse { note =>
          getCoordinates(note.address: String).map { coord =>
            note.copy(coordinates = Some(coord))
          }
        }

      }

    }
}
