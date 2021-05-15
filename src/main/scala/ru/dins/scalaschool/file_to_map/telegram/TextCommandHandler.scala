package ru.dins.scalaschool.file_to_map.telegram

import ru.dins.scalaschool.file_to_map.Config

import scala.util.matching.Regex
//  info - Information about bot
//  help - Available commands
//  settings - Bot settings

object TextCommandHandler {
  def handle(message: String): String = message match {
    case "/deepParse"                 => "" // Extended info about parsing results
    case s"/setLineDel ${d: String}"  => setLineDel(d)
    case s"/setDelInRow ${d: String}" => setDelInRow(d)
    case "/settings"                  => settingsResponse
    case "/info"                      => infoResponse
    case "/help"                      => helpResponse
    case _                            => s"Unknown command '$message'"
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
