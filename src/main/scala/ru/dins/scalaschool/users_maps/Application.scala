package ru.dins.scalaschool.users_maps

import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import doobie.util.transactor.Transactor.Aux
import org.http4s.client.blaze.BlazeClientBuilder
import telegram.TelegramApi
import maps.yandex.YaGeocoder
import org.slf4j.LoggerFactory
import ru.dins.scalaschool.users_maps.storage.{Database, Migrations, PostgresStorage}

import scala.concurrent.ExecutionContext.global

object Application extends IOApp {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def run(args: List[String]): IO[ExitCode] = {
    Migrations.migrate(Database.xa).unsafeRunSync()
    app[IO](Database.xa)
  }

  val myToken: Option[String]      = Some("1792790353:AAE22Vk7SgPNjXaJ8oH5TLqn_d9KWCbBY54")
  val testBotToken: Option[String] = Some("1757140027:AAEswSrV0FWwDyrkDfwo4KAsrIf9zsS6cZw")

  def app[F[_]: ConcurrentEffect: ContextShift](xa: Aux[F, Unit]): F[ExitCode] =
    for {
      _     <- Sync[F].delay(logger.info(s"Starting service"))
      token <- Sync[F].fromOption(testBotToken, new IllegalArgumentException("can't find bot token"))
      _ <- BlazeClientBuilder[F](global).resource
        .use { client =>
          val telegram = TelegramApi(client, token)
          val geocoder = YaGeocoder(client)
          val storage  = PostgresStorage(xa)
          Service.start(telegram, geocoder, storage)
        }
    } yield ExitCode.Success
}
