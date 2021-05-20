package ru.dins.scalaschool

import doobie.util.meta.Meta
import ru.dins.scalaschool.users_maps.Models.UserSettings

package object users_maps {
  val leftPart  = 99
  val rightPart = 101

  val newLine = '\n'

  val defaultLineDelimiter: Char  = newLine
  val defaultInRowDelimiter: Char = ';'
  val defaultNameCol: Int         = 1
  val defaultAddrCol: Int         = 2

  val defaultUserSettings: UserSettings =
    UserSettings(0, Some(defaultLineDelimiter), Some(defaultInRowDelimiter), Some(defaultNameCol), Some(defaultAddrCol))

  implicit val charMeta: Meta[Char] = Meta[String].imap(_.toCharArray.headOption.getOrElse(' '))(_.toString)
}
