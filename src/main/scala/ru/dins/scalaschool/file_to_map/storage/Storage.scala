package ru.dins.scalaschool.file_to_map.storage

import ru.dins.scalaschool.file_to_map.Models.{ErrorMessage, UserSettings}

trait Storage[F[_]] {
  def createUserSettings(us: UserSettings): F[Either[ErrorMessage, UserSettings]]

  def getSettings(chatId: Long): F[Either[ErrorMessage, UserSettings]]

  def setUserSettings(us: UserSettings): F[Either[ErrorMessage, UserSettings]]
}
