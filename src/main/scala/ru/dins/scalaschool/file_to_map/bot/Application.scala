package ru.dins.scalaschool.file_to_map.bot

import cats.effect.{ConcurrentEffect, ExitCode, IO, IOApp, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.http4s.client.blaze.BlazeClientBuilder
import ru.dins.scalaschool.file_to_map.bot.api.TelegramApi
import ru.dins.scalaschool.file_to_map.bot.service.TelegramService

import scala.concurrent.ExecutionContext.global

object Application extends IOApp {
  def run(args: List[String]): IO[ExitCode] = app[IO]()

  val myToken: Option[String] = Some("1792790353:AAE22Vk7SgPNjXaJ8oH5TLqn_d9KWCbBY54")

  def app[F[_]: ConcurrentEffect](): F[ExitCode] =
    for {
      token <- Sync[F].fromOption(myToken, new IllegalArgumentException("can't find bot token"))
      _ <- BlazeClientBuilder[F](global).resource
            .use { client =>
              val telegram = TelegramApi(client, token)
              TelegramService.start(telegram)
            }
    } yield ExitCode.Success
}
