package ru.dins.scalaschool.file_to_map

import cats.Applicative
import cats.effect.{ContextShift, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.circe.syntax.EncoderOps
import org.slf4j.LoggerFactory
import ru.dins.scalaschool.file_to_map.maps.GeocoderApi
import ru.dins.scalaschool.file_to_map.maps.yandex.YaPointToMap._
import ru.dins.scalaschool.file_to_map.maps.yandex.{HtmlHandler, YaPointToMap}
import ru.dins.scalaschool.file_to_map.telegram.TelegramApi
import ru.dins.scalaschool.file_to_map.telegram.model.Chat
import ru.dins.scalaschool.file_to_map.telegram.model.TelegramModel.{Message, UnprocessableUpdate, Update}

import java.io.File

final class Router[F[_]: Applicative] private (routesDefinitions: Router.TelegramUpdateRoute[F[Unit]]*) {
  private val composedRoute: PartialFunction[Update, F[Unit]] =
    routesDefinitions
      .foldLeft(routesDefinitions.head.definition)((all, r) => all.orElse(r.definition))

  def handle(update: Update): F[Unit] = composedRoute.applyOrElse(update, (_: Update) => Applicative[F].unit)
}

object Router {
  private val routerLogger     = LoggerFactory.getLogger("telegram-service")
  private def path(chat: Chat) = s"src/main/resources/usersPoints/chat_${chat.id.toString}_Map.html"

  // represent a way of processing some type of update from user
  final case class TelegramUpdateRoute[O](name: String)(val definition: PartialFunction[Update, O]) {
    override def toString: String = name
  }

  def apply[F[_]: Sync](routes: TelegramUpdateRoute[F[Unit]]*): Router[F] = new Router[F](routes: _*)

  def apply[F[_]: Sync: ContextShift](
      telegram: TelegramApi[F],
      geocoder: GeocoderApi[F],
  ): Router[F] = {
    val messageOnlyRoute: TelegramUpdateRoute[F[Unit]] =
      TelegramUpdateRoute("user-message-only") {
        case Message(Some(user), chat, Some(text), None) =>
          for {
            _ <- Sync[F].delay(routerLogger.info(s"received info from user: $user"))
            _ <- telegram.sendMessage(text, chat)
          } yield ()

        case Message(Some(user), chat, None, Some(document)) =>
          for {
            _       <- Sync[F].delay(routerLogger.info(s"received info from user: $user"))
            file    <- telegram.getFile(document.id)
            content <- telegram.downloadFile(file.path.get)
            notes = FileParser.parse(content)
            enrichedNotes <- geocoder.enrichNotes(notes)
            jsonNotes = enrichedNotes.map(x => YaOneFeature(x))
            _ <- HtmlHandler[F].program(path(chat), fs2.Stream(YaData(features = jsonNotes).asJson.toString()))
            _ <- telegram.sendDocument(chat, new File(path(chat)))
          } yield ()

//        case UnprocessableUpdate(chat) =>
//          for {
//            _ <- telegram.sendMessage("Unprocessable command", chat)
//          } yield ()
      }

    Router(messageOnlyRoute)
  }
}
