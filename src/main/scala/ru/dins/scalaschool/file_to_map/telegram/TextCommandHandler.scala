package ru.dins.scalaschool.file_to_map.telegram

import ru.dins.scalaschool.file_to_map.Config
//  info - Information about bot
//  help - Available commands
//  settings - Bot settings

object TextCommandHandler {
  def handle(message: String): String = message match {
    case s"/setLineDel ${d: String}" => setLineDel(d)
    case s"/setDelInRow ${d: String}" => setDelInRow(d)
    case "/settings" => settingsResponse
    case "/info" => infoResponse
    case "/help" => helpResponse
    case _        => s"Unknown command '$message'"
  }

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

  def setLineDel(d: String): String = {
    Config.lineDelimiter = d
    s"Line delimiter changed to <${Config.lineDelimiter}>"
  }

  def setDelInRow(d: String): String = {
    Config.inRowDelimiter = d
    s"Delimiter in row changed to <${Config.inRowDelimiter}>"
  }

}
