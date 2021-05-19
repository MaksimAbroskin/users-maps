package ru.dins.scalaschool

import ru.dins.scalaschool.users_maps.Models.UserSettings

package object users_maps {
  val newLine = "nl"

  val defaultLineDelimiter: String  = newLine
  val defaultInRowDelimiter: String = ";"
  val defaultNameCol: Int           = 1
  val defaultAddrCol: Int           = 2

  val defaultUserSettings: UserSettings =
    UserSettings(0, Some(defaultLineDelimiter), Some(defaultInRowDelimiter), Some(defaultNameCol), Some(defaultAddrCol))
}
