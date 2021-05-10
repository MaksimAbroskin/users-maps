package ru.dins.scalaschool.file_to_map.maps.yandex

import cats.Applicative
import cats.effect.{ContextShift, Sync}
import cats.implicits.catsSyntaxApplicativeId
import cats.syntax.functor._
import fs2._
import org.http4s._
import org.http4s.client.Client
import org.http4s.implicits._
import ru.dins.scalaschool.file_to_map.bot.api.model.jsonDecoder
import ru.dins.scalaschool.file_to_map.maps.{Coordinates, Geocoder}
import ru.dins.scalaschool.file_to_map.models.Models._

trait YaGeocoder[F[_]] extends Geocoder[F]

object YaGeocoder {
  def apply[F[_]: Sync: ContextShift: Applicative](client: Client[F]): YaGeocoder[F] =
    new YaGeocoder[F] {

      override def getCoordinates(addr: String): F[Coordinates] = {
        val geocoderUri: Uri = uri"""https://geocode-maps.yandex.ru/1.x"""
        val yandexApiKey     = "85e83a9b-10f8-4dd2-98db-47687cb13067"
        //      https://geocode-maps.yandex.ru/1.x?geocode=<addr>&apikey=<yandexApiKey>&format=json&results=1
        val getCoordinatesUri = geocoderUri =? Map(
          "geocode" -> List(addr),
          "apikey"  -> List(yandexApiKey),
          "format"  -> List("json"),
          "results" -> List("1"),
        )

        client
          .expect[YaPoint](Request[F](uri = getCoordinatesUri, method = Method.GET))
          .map(point => Coordinates.coordinatesFromString(point.pos))
      }

      override def getCoordinatesManyPointsStream(in: Stream[F, Note]): Stream[F, F[Note]] =
        in.map { note =>
          for {
            coordinates <- getCoordinates(note.address)
          } yield Note(note.name, note.address, Some(coordinates))
        }

      override def getCoordinatesManyPointsList(in: F[List[Note]]): F[List[F[Note]]] =
        in.map(_.map { note =>
          for {
            coordinates <- getCoordinates(note.address)
          } yield Note(note.name, note.address, Some(coordinates))
        })

      override def getCoordinatesManyPointsList2(in: List[Note]): List[F[Note]] =
        in.map({ note =>
          for {
            coordinates <- getCoordinates(note.address)
          } yield Note(note.name, note.address, Some(coordinates))
        })
    }
}
