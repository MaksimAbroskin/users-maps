package ru.dins.scalaschool.file_to_map

import cats.Applicative
import cats.effect.{ContextShift, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.circe.syntax.EncoderOps
import org.slf4j.LoggerFactory
import ru.dins.scalaschool.file_to_map.maps.GeocoderApi
import ru.dins.scalaschool.file_to_map.maps.yandex.HtmlHandler
import ru.dins.scalaschool.file_to_map.maps.yandex.YaPointToMap.{YaData, YaOneFeature}
import ru.dins.scalaschool.file_to_map.storage.Storage
import ru.dins.scalaschool.file_to_map.telegram.model.Chat
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
      storage: Storage[F]
  ): Router[F] = {
    val messageOnlyRoute: TelegramUpdateRoute[F[Unit]] =
      TelegramUpdateRoute("user-message-only") {
        case Message(Some(user), chat, Some(text), None) =>
          for {
            _ <- Sync[F].delay(println(s"Before creating"))
            create <- storage.createUserSettings(chat.id, "f", "s")
            _ <- Sync[F].delay(println(s"create = $create"))
            _ <- Sync[F].delay(routerLogger.info(s"received info from user: $user"))
            response <- TextCommandHandler.handle(chat.id, text)
            _ <- telegram.sendMessage(response, chat)
          } yield ()

        case Message(Some(user), chat, None, Some(document)) =>
          for {
            _       <- Sync[F].delay(routerLogger.info(s"received info from user: $user"))
            file    <- telegram.getFile(document.id)
            content <- telegram.downloadFile(file.path.get)
            notes = FileParser.parse(content, Config.lineDelimiter, Config.inRowDelimiter)
            _ <- notes match {
              case Left(err) => telegram.sendMessage(err.message, chat)
              case Right(list) =>
                for {
                  _             <- telegram.sendMessage(list._2.message, chat)
                  enrichedNotes <- geocoder.enrichNotes(list._1)
                  _ <- enrichedNotes match {
                    case Left(err) => telegram.sendMessage(err.message, chat)
                    case Right(list) =>
                      val jsonNotes = list._1.map(x => YaOneFeature(x))
                      for {
                        _ <- HtmlHandler[F].program(
                          path(chat),
                          fs2.Stream(YaData(features = jsonNotes).asJson.toString()),
                        )
                        _ <- telegram.sendMessage(list._2.message, chat)
                        _ <- telegram.sendDocument(chat, new File(path(chat)))
                      } yield ()
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
}
