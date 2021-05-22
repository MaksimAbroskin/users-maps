package ru.dins.scalaschool.users_maps

import cats.Applicative
import cats.effect.{ContextShift, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.circe.syntax.EncoderOps
import org.slf4j.LoggerFactory
import ru.dins.scalaschool.users_maps.Config.Http._
import ru.dins.scalaschool.users_maps.Models.NotesWithInfo
import ru.dins.scalaschool.users_maps.maps.GeocoderApi
import ru.dins.scalaschool.users_maps.maps.yandex.YaPointToMap.{YaData, YaOneFeature}
import ru.dins.scalaschool.users_maps.storage.Storage
import ru.dins.scalaschool.users_maps.telegram.model.TelegramModel.{Message, Update}
import ru.dins.scalaschool.users_maps.telegram.model.{Chat, Document}
import ru.dins.scalaschool.users_maps.telegram.{TelegramApi, TextCommandHandler}

import java.util.UUID

final class Router[F[_]: Applicative] private (routesDefinitions: Router.TelegramUpdateRoute[F[Unit]]*) {
  private val composedRoute: PartialFunction[Update, F[Unit]] =
    routesDefinitions
      .foldLeft(routesDefinitions.head.definition)((all, r) => all.orElse(r.definition))

  def handle(update: Update): F[Unit] = composedRoute.applyOrElse(update, (_: Update) => Applicative[F].unit)
}

object Router {
  private val routerLogger     = LoggerFactory.getLogger("telegram-service")
  private def directory(chat: Chat) = s"/home/maps/${chat.id}"
  private def path(directory: String, mapId: String) = s"$directory/$mapId.json"
  private def url(chat: Chat, mapId: String) = s"$host:$port/map.html?chat_id=${chat.id}&map_id=$mapId"

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
        case Message(chat, Some(text), None) =>
          for {
            _ <- Sync[F].delay(routerLogger.info(s"received text message from chat: $chat: $text"))
            _ <- TextCommandHandler.handle(chat, text, storage, telegram, geocoder)
          } yield ()

        case Message(chat, None, Some(document)) =>
          for {
            _           <- Sync[F].delay(routerLogger.info(s"received document from chat: $chat"))
            contentFile <- readUserFile(telegram, document)

            userSettings <- storage.getSettings(chat.id)
            notes =
              StringParser.parse(
                contentFile,
                userSettings match {
                  case Left(err) =>
                    telegram.sendMessage(err.message, chat)
                    defaultUserSettings.copy(chatId = chat.id)
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

        case Message(chat, None, None) =>
          for {
            _ <- Sync[F].delay(routerLogger.info(s"received unprocessed text message from chat: $chat"))
            _ <- telegram.sendMessage("Необрабатываемое сообщение", chat)
          } yield ()
      }

    Router(messageOnlyRoute)
  }

  private def readUserFile[F[_]: Sync](
      telegram: TelegramApi[F],
      document: Document,
  ): F[String] =
    for {
      file    <- telegram.getFile(document.id)
      content <- telegram.downloadFile(file.path.get)
    } yield content

  def createAndSendHtml[F[_]: Sync: ContextShift](
      telegram: TelegramApi[F],
      chat: Chat,
      notesWithInfo: NotesWithInfo,
  ): F[Unit] = {
    val jsonNotes = notesWithInfo.notes.map(x => YaOneFeature(x))
    val mapId = UUID.randomUUID().toString
    val dir = directory(chat)
    for {
      _ <- Utils.createDirectory[F](dir)
      _ <- Utils.createFile[F](
        path(dir, mapId),
        fs2.Stream(YaData(features = jsonNotes).asJson.toString()),
      )
      _ <- telegram.sendMessage(notesWithInfo.info, chat)
      _ <- telegram.sendMessage(url(chat, mapId), chat)
    } yield ()
  }
}
