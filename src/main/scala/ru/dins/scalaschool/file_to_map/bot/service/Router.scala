package ru.dins.scalaschool.file_to_map.bot.service

import cats.Applicative
import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.slf4j.LoggerFactory
import ru.dins.scalaschool.file_to_map.bot.api.TelegramApi
import ru.dins.scalaschool.file_to_map.bot.api.model.TelegramModel.Update
import ru.dins.scalaschool.file_to_map.bot.service.Router.TelegramUpdateRoute

import java.io.File

final class Router[F[_]: Applicative] private (routesDefinitions: TelegramUpdateRoute[F[Unit]]*) {
  private val composedRoute =
    routesDefinitions
      .foldLeft(routesDefinitions.head.definition)((all, r) => all.orElse(r.definition))

  def handle(update: Update): F[Unit] = composedRoute.applyOrElse(update, (_: Update) => Applicative[F].unit)
}

object Router {
  private val routerLogger = LoggerFactory.getLogger("telegram-service")

  final case class TelegramUpdateRoute[O](name: String)(val definition: PartialFunction[Update, O]) {
    override def toString: String = name
  }

  def apply[F[_]: Sync](routes: TelegramUpdateRoute[F[Unit]]*): Router[F] = new Router[F](routes: _*)

  // define a router in terms of telegram API
  def ofTelegramApi[F[_]: Sync](telegram: TelegramApi[F]): Router[F] = {
    val messageOnlyRoute =
      TelegramUpdateRoute("user-message-only") {
        case Update.Message(Some(user), chat, Some(text), None) =>
          for {
            _ <- Sync[F].delay(routerLogger.info(s"received info from user: $user"))
            _ <- telegram.sendMessage(text, chat)
            point <- telegram.getCoordinates(text)
            _ <- telegram.sendMessage(point.toString, chat)
            f: File = new File("src/resources/gisTest.html")
            _ <- telegram.sendDocument(chat, f)
          } yield ()

        case Update.Message(Some(user), chat, None, Some(document)) =>
          for {
            _    <- Sync[F].delay(routerLogger.info(s"received info from user: $user"))
            file <- telegram.getFile(document.id)
            content <- telegram.downloadFile(file.path.get)
            _    <- telegram.sendMessage(s"Content of document: \n$content", chat)
          } yield ()
      }

    Router(messageOnlyRoute)
  }
}
