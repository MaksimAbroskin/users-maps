package ru.dins.scalaschool.users_maps.telegram

import cats.effect.{ContextShift, Sync}
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.functor._
import fs2._
import org.http4s._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.implicits._
import org.slf4j.LoggerFactory
import ru.dins.scalaschool.users_maps.telegram.model.TelegramModel.TelegramModelDecoders._
import ru.dins.scalaschool.users_maps.telegram.model.TelegramModel.{Success, Update}
import ru.dins.scalaschool.users_maps.telegram.model.{Chat, Offset, TelegramModel, File => myFile}

trait TelegramApi[F[_]] extends Http4sClientDsl[F] {

  def getUpdates(startFrom: Offset): Stream[F, Update]

  def sendMessage(text: String, chat: Chat): F[Unit]

  def getFile(id: String): F[myFile]

  def downloadFile(path: String): F[String]
}

object TelegramApi {
  private val logger = LoggerFactory.getLogger("telegram-api")

  def apply[F[_]: Sync: ContextShift](client: Client[F], secret: String): TelegramApi[F] = new TelegramApi[F] {
    val uri: Uri = uri"""https://api.telegram.org""" / s"bot$secret"

    val getUpdatesEndpoint: Uri = uri / "getUpdates" =? Map(
      "timeout"         -> List("30"),
      "allowed_updates" -> List("""["message"]"""),
    )

    override def getUpdates(startFrom: Offset): Stream[F, Update] = {
      def getListUpdates(offset: Offset): F[List[Success[Update]]] =
        client
          .expect[TelegramModel.Response](
            Request[F](uri = getUpdatesEndpoint +? ("offset", offset.value), method = Method.GET),
          )
          .flatMap { response =>
            response.data match {
              case Left(flr)      => Sync[F].delay(logger.info(s"received failure from telegram: $flr")).as(List.empty)
              case Right(updates) => updates.pure[F]
            }
          }

      def loop(o: Offset): Stream[F, Update] =
        Stream
          .eval(getListUpdates(o))
          .flatMap { updates =>
            if (updates.isEmpty) loop(o)
            else Stream.emits(updates.map(_.data)) ++ loop(updates.maxBy(_.offset).offset.next)
          }

      loop(startFrom)
    }

    override def sendMessage(text: String, chat: Chat): F[Unit] = {
      val endpoint       = uri / "sendMessage"
      val sendMessageUri = endpoint =? Map("chat_id" -> List(chat.id.toString), "text" -> List(text))

      client.expect[Unit](Request[F](uri = sendMessageUri, method = Method.GET))
    }

    override def getFile(id: String): F[myFile] = {
      val endpoint   = uri / "getFile"
      val getFileUri = endpoint =? Map("file_id" -> List(id))

      client.expect[myFile](Request[F](uri = getFileUri, method = Method.GET))
    }

    override def downloadFile(path: String): F[String] = {
      //https://api.telegram.org/file/bot<token>/<file_path>
      val endpoint = uri"""https://api.telegram.org""" / "file" / s"bot$secret" / path

      client.expect[String](Request[F](uri = endpoint, method = Method.GET))
    }
  }
}
