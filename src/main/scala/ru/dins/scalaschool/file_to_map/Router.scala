package ru.dins.scalaschool.file_to_map

import cats.Applicative
import cats.effect.{ContextShift, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.circe.syntax.EncoderOps
import org.slf4j.LoggerFactory
import ru.dins.scalaschool.file_to_map.Models.{NotesWithInfo, UserSettings}
import ru.dins.scalaschool.file_to_map.maps.GeocoderApi
import ru.dins.scalaschool.file_to_map.maps.yandex.HtmlHandler
import ru.dins.scalaschool.file_to_map.maps.yandex.YaPointToMap.{YaData, YaOneFeature}
import ru.dins.scalaschool.file_to_map.storage.Storage
import ru.dins.scalaschool.file_to_map.telegram.model.{Chat, Document}
import ru.dins.scalaschool.file_to_map.telegram.model.TelegramModel.{Message, Update}
import ru.dins.scalaschool.file_to_map.telegram.{TelegramApi, TextCommandHandler}

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
      storage: Storage[F],
  ): Router[F] = {
    val messageOnlyRoute: TelegramUpdateRoute[F[Unit]] =
      TelegramUpdateRoute("user-message-only") {
        case Message(Some(user), chat, Some(text), None) =>
          for {
            _ <- Sync[F].delay(routerLogger.info(s"received info from user: $user"))
            _ <- TextCommandHandler.handle(chat, text, storage, telegram, geocoder)
          } yield ()

        case Message(Some(user), chat, None, Some(document)) =>
          for {
            _           <- Sync[F].delay(routerLogger.info(s"received info from user: $user"))
            contentFile <- readUserFile(telegram, storage, document, chat.id)

            userSettings <- storage.getSettings(chat.id)
            notes =
              StringParser.parse(
                contentFile,
                userSettings match {
                  case Left(err) =>
                    telegram.sendMessage(err.message, chat)
                    Config.defaultUserSettings.copy(chatId = chat.id)
                  case Right(userSettings) => userSettings
                },
              )
            _ <- notes match {
              case Left(err) => telegram.sendMessage(err.message, chat)
              case Right(notesWithInfo) =>
                for {
                  _             <- telegram.sendMessage(notesWithInfo.info, chat)
                  enrichedNotes <- geocoder.enrichNotes(notesWithInfo.notes)
                  _ <- enrichedNotes match {
                    case Left(err)            => telegram.sendMessage(err.message, chat)
                    case Right(notesWithInfo) => createAndSendHtml(telegram, chat, notesWithInfo)
                  }
                } yield ()
            }
          } yield ()

        case Message(Some(user), chat, None, None) =>
          for {
            _ <- Sync[F].delay(routerLogger.info(s"received info from user: $user"))
            _ <- telegram.sendMessage("Unprocessable message", chat)
          } yield ()
      }

    Router(messageOnlyRoute)
  }

  private def readUserFile[F[_]: Sync](
      telegram: TelegramApi[F],
      storage: Storage[F],
      document: Document,
      chatId: Long,
  ): F[String] =
    for {
      file    <- telegram.getFile(document.id)
      _       <- storage.setUserSettings(UserSettings(chatId = chatId, lastFileId = Some(file.path.get)))
      content <- telegram.downloadFile(file.path.get)
    } yield content

  def createAndSendHtml[F[_]: Sync: ContextShift](
      telegram: TelegramApi[F],
      chat: Chat,
      notesWithInfo: NotesWithInfo,
  ): F[Unit] = {
    val jsonNotes = notesWithInfo.notes.map(x => YaOneFeature(x))
    for {
      _ <- HtmlHandler[F].createFile(
        path(chat),
        fs2.Stream(YaData(features = jsonNotes).asJson.toString()),
      )
      _ <- telegram.sendMessage(notesWithInfo.info, chat)
      _ <- telegram.sendDocument(chat, new File(path(chat)))
    } yield ()
  }
}
