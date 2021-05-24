package ru.dins.scalaschool.users_maps.maps.yandex

import cats.effect.{ContextShift, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import ru.dins.scalaschool.users_maps
import ru.dins.scalaschool.users_maps.Router.createFileAndSendLink
import ru.dins.scalaschool.users_maps.maps.GeocoderApi
import ru.dins.scalaschool.users_maps.storage.Storage
import ru.dins.scalaschool.users_maps.telegram.TelegramApi
import ru.dins.scalaschool.users_maps.telegram.model.Chat
import ru.dins.scalaschool.users_maps.StringParser

final case class HtmlHandler[F[_]: Sync: ContextShift]() {
  def stringToHtml(
      telegram: TelegramApi[F],
      geocoder: GeocoderApi[F],
      storage: Storage[F],
      chat: Chat,
      content: String,
  ): F[Unit] =
    for {
      userSettings <- storage.getSettings(chat.id)
      notes =
        StringParser.parse(
          content,
          userSettings match {
            case Left(err) =>
              telegram.sendMessage(err.message, chat)
              users_maps.defaultUserSettings.copy(chatId = chat.id)
            case Right(us) => us
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
              case Right(notesWithInfo) => createFileAndSendLink(telegram, chat, notesWithInfo)
            }
          } yield ()
      }
    } yield ()

}
