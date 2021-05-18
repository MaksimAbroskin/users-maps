package ru.dins.scalaschool.file_to_map

import ru.dins.scalaschool.file_to_map.Models.UserSettings

object Config {
  val newLine = "nl"
  val lineDelimiter   = "nl"
  val inRowDelimiter = ";"
  val defaultUserSettings: UserSettings = UserSettings(0, Some(lineDelimiter), Some(inRowDelimiter), Some(1), Some(2))
}
