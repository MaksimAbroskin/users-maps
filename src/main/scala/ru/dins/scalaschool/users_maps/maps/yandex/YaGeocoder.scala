package ru.dins.scalaschool.users_maps.maps.yandex

import cats.effect.Sync
import cats.syntax.functor._
import cats.syntax.applicativeError._
import org.http4s.Status.Successful
import org.http4s._
import org.http4s.circe.toMessageSynax
import org.http4s.client.Client
import org.http4s.implicits._
import ru.dins.scalaschool.users_maps.Models._
import ru.dins.scalaschool.users_maps.maps.{Coordinates, GeocoderApi}

trait YaGeocoder[F[_]] extends GeocoderApi[F]

object YaGeocoder {
  def apply[F[_]: Sync](client: Client[F]): YaGeocoder[F] =
    new YaGeocoder[F] {

      val geocoderUri: Uri = uri"""https://geocode-maps.yandex.ru/1.x"""
      val yandexApiKey     = "85e83a9b-10f8-4dd2-98db-47687cb13067"
      //      https://geocode-maps.yandex.ru/1.x?geocode=<addr>&apikey=<yandexApiKey>&format=json&results=1
      private def getCoordinatesUri(addr: String): Uri = geocoderUri =? Map(
        "geocode" -> List(addr),
        "apikey"  -> List(yandexApiKey),
        "format"  -> List("json"),
        "results" -> List("1"),
      )

      private def getCoordinates(addr: String): F[Option[Coordinates]] =
        client.get(getCoordinatesUri(addr)) {
          case Successful(resp) =>
            resp
              .decodeJson[YaPoint]
              .map(point => Option(Coordinates.coordinatesFromString(point.pos)))
              .handleErrorWith(_ => Sync[F].delay(None))
          case _ => Sync[F].delay(None)
        }

      private def geocodeReport(success: Int, total: Int) = s"Координаты получены.\nУспешно: $success из $total"

      override def enrichNotes(in: List[Note]): F[Either[ErrorMessage, NotesWithInfo]] = {
        import cats.implicits._
        val oneTraverse = in.traverse { note =>
          getCoordinates(note.address: String).map(coord => note.copy(coordinates = coord))
        }
        for {
          filtered <- oneTraverse.map(_.filter(_.coordinates.isDefined))
          result =
            if (filtered.isEmpty) Left(YaGeocoderError())
            else Right(NotesWithInfo(filtered, geocodeReport(filtered.length, in.length)))
        } yield result
      }
    }
}
