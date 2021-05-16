package ru.dins.scalaschool.file_to_map.storage

import ru.dins.scalaschool.file_to_map.Models.{ErrorMessage, UserSettings}

trait Storage[F[_]] {
  def createUserSettings(chatId: Long, lineDelimiter: String, inRowDelimiter: String): F[Either[ErrorMessage, UserSettings]]

  def getSettings(chatId: Long): F[Either[ErrorMessage, UserSettings]]

  def setLineDelimiter(chatId: Long, newDelimiter: String): F[Either[ErrorMessage, UserSettings]]

  def setInRowDelimiter(chatId: Long, newDelimiter: String): F[Either[ErrorMessage, UserSettings]]

  def setLastFileId(chatId: Long, newFileId: String): F[Either[ErrorMessage, UserSettings]]

  def setDataModel(chatId: Long, nameCol: Option[Int], addrCol: Option[Int], infoCol: Option[Int]): F[Either[ErrorMessage, UserSettings]]
}
