package ru.dins.scalaschool.file_to_map.client

import ru.dins.scalaschool.file_to_map.bot.api.TelegramApi
import ru.dins.scalaschool.file_to_map.bot.api.model.{Chat, File, Offset, TelegramModel}
import ru.dins.scalaschool.file_to_map.maps.{Coordinates, Geocoder}

import java.io.File

//trait Client[F[_]] extends TelegramApi[F] with Geocoder[F] {}

//object Client {
//  def apply[F[_]](): Client[F] = new Client[F] {
//    override def getCoordinates(addr: String): F[Coordinates] = ???
//
//    override def getUpdates(startFrom: Offset): fs2.Stream[F, TelegramModel.Update] = ???
//
//    override def sendMessage(text: String, chat: Chat): F[Unit] = ???
//
////    override def getFile(id: String): F[File] = ???
//
//    override def downloadFile(path: String): F[String] = ???
//
////    override def sendDocument(chat: Chat, document: File): F[Unit] = ???
//  }
//}
