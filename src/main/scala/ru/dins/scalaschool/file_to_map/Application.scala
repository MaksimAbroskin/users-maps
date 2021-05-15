package ru.dins.scalaschool.file_to_map

import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import doobie.util.transactor.Transactor.Aux
import org.http4s.client.blaze.BlazeClientBuilder
import telegram.TelegramApi
import maps.yandex.YaGeocoder
import ru.dins.scalaschool.file_to_map.storage.{Database, Migrations, PostgresStorage}

import scala.concurrent.ExecutionContext.global

object Application extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    Migrations.migrate(Database.xa).unsafeRunSync()
    app[IO](Database.xa)
  }

  val myToken: Option[String] = Some("1792790353:AAE22Vk7SgPNjXaJ8oH5TLqn_d9KWCbBY54")

  def app[F[_]: ConcurrentEffect: ContextShift](xa: Aux[F, Unit]): F[ExitCode] =
    for {
      token <- Sync[F].fromOption(myToken, new IllegalArgumentException("can't find bot token"))
      _ <- BlazeClientBuilder[F](global).resource
        .use { client =>
          val telegram = TelegramApi(client, token)
          val geocoder = YaGeocoder(client)
          val storage = PostgresStorage(xa)
          Service.start(telegram, geocoder, storage)
        }
    } yield ExitCode.Success
}
