package ru.dins.scalaschool.users_maps.storage

import ru.dins.scalaschool.users_maps.Models.{ErrorMessage, UserSettings}

trait Storage[F[_]] {
  def createUserSettings(us: UserSettings): F[Either[ErrorMessage, UserSettings]]

  def getSettings(chatId: Long): F[Either[ErrorMessage, UserSettings]]

  def setUserSettings(us: UserSettings): F[Either[ErrorMessage, UserSettings]]
}
