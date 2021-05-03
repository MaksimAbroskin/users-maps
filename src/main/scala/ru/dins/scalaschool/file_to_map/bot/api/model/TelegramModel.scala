package ru.dins.scalaschool.file_to_map.bot.api.model

import cats.syntax.either._
import io.circe._

object TelegramModel {
  final case class Response(data: Either[Failure, List[Success[Update]]])
  final case class Failure(code: Int, description: String) extends Exception(s"code=$code, description=$description")
  final case class Success[+A](offset: Offset, data: A)

  sealed trait Update
  object Update {
    final case class Message(user: Option[User], chat: Chat, text: Option[String], document: Option[Document]) extends Update
    // other update types
  }

  object TelegramModelDecoders {
    implicit val messageDecoder: Decoder[Update.Message] =
      (c: HCursor) => {
        for {
          user <- c.get[Option[User]]("from")
          chat <- c.get[Chat]("chat")
          text <- c.get[Option[String]]("text")
          document <- c.get[Option[Document]]("document")
        } yield Update.Message(user, chat, text, document)
      }

    implicit val successDecoder: Decoder[Success[Update.Message]] = { (c: HCursor) =>
      for {
        offset <- c.get[Offset]("update_id")
        update <- c.get[Update.Message]("message")
      } yield Success(offset, update)
    }

    implicit val failureDecoder: Decoder[Failure] = { (c: HCursor) =>
      for {
        code <- c.get[Int]("error_code")
        desc <- c.get[String]("description")
      } yield Failure(code, desc)
    }

    implicit val updateDecoder: Decoder[Response] = { (c: HCursor) =>
      c.get[Boolean]("ok")
        .flatMap(isOk =>
          if (isOk) c.downField("result").as[List[Success[Update.Message]]].map(sc => Response(sc.asRight))
          else c.as[Failure].map(flr => Response(flr.asLeft))
        )
    }
  }
}
