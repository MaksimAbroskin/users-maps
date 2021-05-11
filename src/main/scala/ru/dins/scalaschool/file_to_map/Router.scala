package ru.dins.scalaschool.file_to_map

import cats.Applicative
import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.slf4j.LoggerFactory
import telegram.TelegramApi
import telegram.model.TelegramModel.{Message, Update}
import maps.GeocoderApi

import java.io.File

final class Router[F[_]: Applicative] private (routesDefinitions: Router.TelegramUpdateRoute[F[Unit]]*) {
  private val composedRoute: PartialFunction[Update, F[Unit]] =
    routesDefinitions
      .foldLeft(routesDefinitions.head.definition)((all, r) => all.orElse(r.definition))

  def handle(update: Update): F[Unit] = composedRoute.applyOrElse(update, (_: Update) => Applicative[F].unit)
}

object Router {
  private val routerLogger = LoggerFactory.getLogger("telegram-service")

  // represent a way of processing some type of update from user
  final case class TelegramUpdateRoute[O](name: String)(val definition: PartialFunction[Update, O]) {
    override def toString: String = name
  }

  def apply[F[_]: Sync](routes: TelegramUpdateRoute[F[Unit]]*): Router[F] = new Router[F](routes: _*)

  def apply[F[_]: Sync](
                         telegram: TelegramApi[F],
                         geocoder: GeocoderApi[F],
  ): Router[F] = {
    val messageOnlyRoute: TelegramUpdateRoute[F[Unit]] =
      TelegramUpdateRoute("user-message-only") {
//        case Message(Some(user), chat, Some(text), None) =>
//          for {
//            _ <- Sync[F].delay(routerLogger.info(s"received info from user: $user"))
//            _ <- telegram.sendMessage(text, chat)
//            f: File = new File("src/resources/gisTest.html")
//            _ <- telegram.sendDocument(chat, f)
//          } yield ()

        case Message(Some(user), chat, None, Some(document)) =>
          for {
            _               <- Sync[F].delay(routerLogger.info(s"received info from user: $user"))
            file            <- telegram.getFile(document.id)
            content         <- telegram.downloadFile(file.path.get)
            notes =         FileParser.parse(content)
            enrichedNotes   <- geocoder.enrichNotes(notes)
            _               <- Sync[F].delay(enrichedNotes.foreach(note => println(s"note = $note")))
            _ <- telegram.sendDocument(chat, new File("src/main/resources/object_manager.html"))
          } yield ()
      }

    Router(messageOnlyRoute)
  }
}
