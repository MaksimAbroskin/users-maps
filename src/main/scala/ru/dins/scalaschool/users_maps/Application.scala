package ru.dins.scalaschool.users_maps

import cats.effect.{Blocker, ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Sync, Timer}
import cats.syntax.flatMap._
import cats.syntax.functor._
import doobie.util.transactor.Transactor.Aux
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.staticcontent.{FileService, fileService}
import org.slf4j.LoggerFactory
import ru.dins.scalaschool.users_maps.maps.yandex.YaGeocoder
import ru.dins.scalaschool.users_maps.storage.{Database, PostgresStorage}
import ru.dins.scalaschool.users_maps.telegram.TelegramApi
import Config.Http._
import org.flywaydb.core.Flyway

object Application extends IOApp {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def run(args: List[String]): IO[ExitCode] = {
    implicit val blocker: Blocker = Blocker.liftExecutionContext(executionContext)

    Flyway
      .configure()
      .dataSource(s"jdbc:postgresql://${Config.Postgres.host}:${Config.Postgres.port}/${Config.Postgres.name}", Config.Postgres.user, Config.Postgres.pass)
      .locations("db/migration")
      .baselineOnMigrate(true)
      .load()
      .migrate()

//    Migrations.migrate(Database.xa).unsafeRunSync()
    app[IO](Database.xa)
  }

  val localTestToken: Option[String] = Some("1757140027:AAEswSrV0FWwDyrkDfwo4KAsrIf9zsS6cZw")
  val productToken: Option[String] = Some("1792790353:AAE22Vk7SgPNjXaJ8oH5TLqn_d9KWCbBY54")

  val addedToken: Option[String] = Some("1743888075:AAEd2HQFfU2nvN3qB5G-diDn5JCBHrpajJ0")

  def app[F[_]: ConcurrentEffect: ContextShift: Timer](xa: Aux[F, Unit])(implicit blocker: Blocker): F[ExitCode] =
    for {
      _     <- Sync[F].delay(logger.info(s"Starting service"))
      token <- Sync[F].fromOption(localTestToken, new IllegalArgumentException("can't find bot token"))
      _ <- Utils.copyFile[F]("/opt/docker/resources/map.html", "/home/maps/map.html")
      _ <- (for {
        _ <- BlazeServerBuilder[F](executionContext)
          .bindHttp(host = localHost, port = port)
          .withHttpApp(fileService[F](FileService.Config("/home/maps", blocker)).orNotFound)
          .resource
        client <- BlazeClientBuilder[F](executionContext).resource
      } yield client).use { client =>
        val telegram = TelegramApi(client, token)
        val geocoder = YaGeocoder(client)
        val storage  = PostgresStorage(xa)
        Service.start(telegram, geocoder, storage)
      }
    } yield ExitCode.Success
}
