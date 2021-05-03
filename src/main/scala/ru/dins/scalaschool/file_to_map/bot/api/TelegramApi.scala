package ru.dins.scalaschool.file_to_map.bot.api

import fs2._
import org.http4s._
import org.http4s.implicits._
import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.applicative._
import org.slf4j.LoggerFactory
import org.http4s.client.Client
import ru.dins.scalaschool.file_to_map.bot.api.model.{Chat, File, Offset, TelegramModel}
import ru.dins.scalaschool.file_to_map.bot.api.model.TelegramModel.{Success, Update}
import ru.dins.scalaschool.file_to_map.bot.api.model.TelegramModel.TelegramModelDecoders._

trait TelegramApi[F[_]] {

  /**
    * Stream of updates starting from offset [[ru.dins.scalaschool.api.model.Offset]]
    *
    * @param startFrom offset to start from
    * @return stream of updates
    */
  def getUpdates(startFrom: Offset): Stream[F, Update]

  /**
    * Send a text message
    *
    * @param text text to send
    * @param chat destination chat
    * @return
    */
  def sendMessage(text: String, chat: Chat): F[Unit]

  def getFile(id: String): F[File]

  def downloadFile(path: String): F[String]
}

object TelegramApi {
  private val logger = LoggerFactory.getLogger("telegram-api")

  def apply[F[_]: Sync](client: Client[F], secret: String): TelegramApi[F] = new TelegramApi[F] {
    val uri: Uri = uri"""https://api.telegram.org""" / s"bot$secret"

    override def getUpdates(startFrom: Offset): Stream[F, Update] = {
      val endpoint = uri / "getUpdates" =? Map(
          "timeout"         -> List("30")
        , "allowed_updates" -> List("""["message"]""")
      )

      def getListUpdates(offset: Offset): F[List[Success[Update]]] =
        client
          .expect[TelegramModel.Response](Request[F](uri = endpoint +? ("offset", offset.value), method = Method.GET))
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

      // todo parse response and retry on failure
      client.expect[Unit](Request[F](uri = sendMessageUri, method = Method.GET))
    }

    override def getFile(id: String): F[File] = {
      val endpoint       = uri / "getFile"
      val getFileUri = endpoint =? Map("file_id" -> List(id))

      client.expect[File](Request[F](uri = getFileUri, method = Method.GET))
    }

    override def downloadFile(path: String): F[String] = {
      //https://api.telegram.org/file/bot<token>/<file_path>
      val endpoint       = uri"""https://api.telegram.org""" / "file" / s"bot$secret" / path

      client.expect[String](Request[F](uri = endpoint, method = Method.GET))
    }
  }
}
