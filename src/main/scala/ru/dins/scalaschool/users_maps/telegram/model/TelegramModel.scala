package ru.dins.scalaschool.users_maps.telegram.model

import cats.syntax.either._
import io.circe._
import io.circe.generic.semiauto.deriveDecoder

object TelegramModel {
  final case class Response(data: Either[Failure, List[Success[Update]]])
  final case class Failure(code: Int, description: String) extends Exception(s"code=$code, description=$description")
  final case class Success[+A](offset: Offset, data: A)

  sealed trait Update
  case class Message(chat: Chat, text: Option[String], document: Option[Document]) extends Update

  object TelegramModelDecoders {
    implicit val messageDecoder: Decoder[Message] = deriveDecoder

    implicit val successDecoder: Decoder[Success[Message]] = { (c: HCursor) =>
      for {
        offset <- c.get[Offset]("update_id")
        update <- c.get[Message]("message")
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
          if (isOk) c.downField("result").as[List[Success[Message]]].map(sc => Response(sc.asRight))
          else c.as[Failure].map(flr => Response(flr.asLeft))
        )
    }
  }
}
