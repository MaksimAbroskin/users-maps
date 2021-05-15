package ru.dins.scalaschool.file_to_map.telegram

import cats.effect.{IO, Sync}
import ru.dins.scalaschool.file_to_map.Config
import ru.dins.scalaschool.file_to_map.Models.UserSettings
import ru.dins.scalaschool.file_to_map.storage.{Database, PostgresStorage}

import scala.util.matching.Regex
//  info - Information about bot
//  help - Available commands
//  settings - Bot settings

object TextCommandHandler {
  val storage: PostgresStorage[IO] = PostgresStorage(Database.xa)
  def handle[F[_]: Sync](chatId: Long, message: String): F[String] = message match {
    case "/deepParse"                 => Sync[F].delay("") // Extended info about parsing results
    case s"/setLineDel ${d: String}"  => Sync[F].delay(setLineDel(d))
    case s"/setDelInRow ${d: String}" => Sync[F].delay(setDelInRow(d))
    case "/settings"                  => Sync[F].delay(settingsResponse)
    case "/info"                      => Sync[F].delay(infoResponse)
    case "/help"                      => Sync[F].delay(helpResponse)
    case _                            => Sync[F].delay(s"Unknown command '$message'")
  }

  val regex: Regex = "\\W{1}".r

  val infoResponse: String =
    """Information about bot:
      |   version: 1.0
      |   author: Abroskin Maksim
      |   mentor: Leonid Gumenyuk""".stripMargin

  val helpResponse: String =
    """Available commands:
      |   /info - information about bot
      |   /help - show commands list
      |
      |   """.stripMargin

  val settingsResponse: String =
    s"""Current settings:
       |  - line delimiter - <${Config.lineDelimiter}>
       |  - delimiter in line - <${Config.inRowDelimiter}>
       |  - another settings .......""".stripMargin

  def setLineDel(d: String): String =
    if (regex.matches(d)) {
      Config.lineDelimiter = d
      s"Line delimiter changed to <${Config.lineDelimiter}>"
    } else "Incorrect delimiter! It should be ONE symbol, no digit or letter"

  def setDelInRow(d: String): String =
    if (regex.matches(d)) {
      Config.inRowDelimiter = d
      s"Delimiter in row changed to <${Config.inRowDelimiter}>"
    } else "Incorrect delimiter! It should be ONE symbol, no digit or letter"

}
