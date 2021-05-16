package ru.dins.scalaschool.file_to_map.telegram

import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import ru.dins.scalaschool.file_to_map.Config
import ru.dins.scalaschool.file_to_map.storage.Storage
import ru.dins.scalaschool.file_to_map.telegram.model.Chat

import scala.util.Try
import scala.util.matching.Regex
//start - Initialize bot. If this is your first time
//settings - Settings of your bot
//set_line_del - Set new line delimiter
//set_del_in_row - Set new delimiter in row
//help - Available commands

object TextCommandHandler {

  private val regexLine: Regex   = ("\\W{1}|" + Config.newLine).r
  private val regexInLine: Regex = "\\W{1}".r
  private val incorrectDelimiter = "Incorrect delimiter! It should be ONE symbol, no digit or letter"

  def handle[F[_]: Sync](chat: Chat, message: String, storage: Storage[F], telegram: TelegramApi[F]): F[Unit] =
    message match {
      case "/start" =>
        for {
          create <- storage.createUserSettings(chat.id, Config.lineDelimiter, Config.inRowDelimiter)
          _ <- create match {
            case Left(err) => telegram.sendMessage(err.message, chat)
            case Right(_)  => telegram.sendMessage(startMessage, chat)
          }
        } yield ()

      case "/deepParse" => telegram.sendMessage("Joke)", chat)

      case s"/set_line_del ${d: String}" =>
        if (regexLine.matches(d))
          for {
            settings <- storage.setLineDelimiter(chat.id, d)
            _ <- settings match {
              case Left(err) => telegram.sendMessage(err.message, chat)
              case Right(s)  => telegram.sendMessage(s"${s.lineDelimiter} - is your new line delimiter", chat)
            }
          } yield ()
        else telegram.sendMessage(incorrectDelimiter, chat)

      case s"/set_del_in_row ${d: String}" =>
        if (regexInLine.matches(d))
          for {
            settings <- storage.setInRowDelimiter(chat.id, d)
            _ <- settings match {
              case Left(err) => telegram.sendMessage(err.message, chat)
              case Right(s)  => telegram.sendMessage(s"${s.inRowDelimiter} - is your new delimiter in rows", chat)
            }
          } yield ()
        else telegram.sendMessage(incorrectDelimiter, chat)

      case s"/set_data_model ${model: String}" =>
        parseDataModel(model) match {
          case Some(optList) =>
            optList match {
              case name :: address :: info :: Nil =>
                for {
                  settings <- storage.setDataModel(chat.id, name, address, info)
                  _ <- settings match {
                    case Left(err) => telegram.sendMessage(err.message, chat)
                    case Right(_)  => telegram.sendMessage(s"Data model updated successfully", chat)
                  }
                } yield ()
              case name :: address :: Nil =>
                for {
                  settings <- storage.setDataModel(chat.id, name, address, None)
                  _ <- settings match {
                    case Left(err) => telegram.sendMessage(err.message, chat)
                    case Right(_)  => telegram.sendMessage(s"Data model updated successfully", chat)
                  }
                } yield ()
              case _ => telegram.sendMessage("Error while parsing data model", chat)
            }
//            for {
//              settings <- storage.setDataModel(chat.id, model)
//              _ <- settings match {
//                case Left(err) => telegram.sendMessage(err.message, chat)
//                case Right(s)  => telegram.sendMessage(s"Data model updated successfully", chat)
//              }
//            } yield ()
          case None =>
            telegram.sendMessage(
              "Incorrect data model!\nModel should be:\n<Name's column(Int)> <Address's column(Int)> <Info's column(Int)(Optional)>",
              chat,
            )
        }

      case "/settings" =>
        for {
          settings <- storage.getSettings(chat.id)
          _ <- settings match {
            case Left(err)       => telegram.sendMessage(err.message, chat)
            case Right(settings) => telegram.sendMessage(settings.message, chat)
          }
        } yield ()

      case "/info_set_line_del" => telegram.sendMessage(setLineDelDescription, chat)

      case "/info_set_del_in_row" => telegram.sendMessage(setDelInRowDescription, chat)

      case "/info_set_data_model" => telegram.sendMessage(setDataModelDescription, chat)

      case s"/set_line_del" =>
        telegram.sendMessage("This command must have the parameter. More information /info_set_line_del", chat)

      case s"/set_del_in_row" =>
        telegram.sendMessage("This command must have the parameter. More information /info_set_del_in_row", chat)

      case "/help" => telegram.sendMessage(helpResponse, chat)
      case _       => telegram.sendMessage(s"Unknown command '$message'", chat)
    }

//  private def checkDataModel(model: String): Boolean = {
//    val sList = model.split("\\s+").toList
//    if (sList.length != sList.distinct.length | sList.length < 2 | sList.length > 3) false
//    else {
//      val optList = sList.map(x => Try[Int](x.toInt).toOption)
//      if (optList.contains(None)) false
//      else true
//    }
//  }

  private def parseDataModel(model: String): Option[List[Option[Int]]] = {
    val sList = model.split("\\s+").toList
    if (sList.length != sList.distinct.length | sList.length < 2 | sList.length > 3) None
    else {
      val optList = sList.map(x => Try[Int](x.toInt).toOption)
      if (optList.contains(None)) None
      else Some(optList)
    }
  }

  private val helpResponse =
    """Available commands:
      |   /start - Initialize bot. If this is your first time
      |   /settings - Settings of your bot
      |   set_data_model - configure position of data in your file. More Information /info_set_data_model
      |   set_line_del <new_delimiter> - Set new line delimiter. More information /info_set_line_del
      |   set_del_in_row <new_delimiter> - Set new delimiter in row. More information /info_set_del_in_row
      |   /help - Available commands
      |   """.stripMargin

  private val setLineDelDescription =
    """It's command for change line delimiter.
      |Use ONE symbol, no digit or letter.
      |If you want to use end of line as delimiter, use "\n".
      |Examples:
      |   /set_line_del \n
      |   /set_line_del ;""".stripMargin

  private val setDelInRowDescription =
    """It's command for change delimiter between fields in the line.
      |Use ONE symbol, no digit or letter.
      |Examples:
      |   /set_del_in_row '
      |   /set_del_in_row :""".stripMargin

  private val setDataModelDescription =
    """It's command for set positions of data in your file.
      |   Command syntax:
      |   /set_data_model <name(Int)> <address(Int)> <info(Int)(Optional)>
      |   where <name> and <address> - required fields with numbers of columns, which contain corresponding data
      |   <info> - optional field, with number of column, which contain additional info about point
      |Examples:
      |   /set_data_model 1 2
      |   /set_data_model 2 1 4
      |""".stripMargin

  private val startMessage =
    """Hello! I'm bot for creating maps from your file.
      |For successful work this file have to contain at least 2 fields:
      |   - address of point
      |   - name of point""".stripMargin

}
