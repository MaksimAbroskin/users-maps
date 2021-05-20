//package ru.dins.scalaschool.users_maps.telegram
//
//import cats.effect.{ContextShift, Sync}
//import cats.syntax.flatMap._
//import cats.syntax.functor._
//import ru.dins.scalaschool.users_maps.Models.UserSettings
//import ru.dins.scalaschool.users_maps._
//import ru.dins.scalaschool.users_maps.maps.GeocoderApi
//import ru.dins.scalaschool.users_maps.maps.yandex.HtmlHandler
//import ru.dins.scalaschool.users_maps.storage.Storage
//import ru.dins.scalaschool.users_maps.telegram.model.Chat
//
//import scala.util.Try
//import scala.util.matching.Regex
////start - Initialize bot. If this is your first time
////settings - Settings of your bot
////set_line_del - Set new line delimiter
////set_del_in_row - Set new delimiter in row
////set_data_model - Set data model of your file
////help - Available commands
//
//object TextCommandHandler {
//
//  private val regexLine: Regex   = ("""[^\w(\\]{1}|""" + newLine).r
//  private val regexInLine: Regex = """[^\w(\\]{1}""".r
//  private val incorrectDelimiter = "Incorrect delimiter! It should be ONE symbol, no digit or letter"
//
//  def handle[F[_]: Sync: ContextShift](
//      chat: Chat,
//      message: String,
//      storage: Storage[F],
//      telegram: TelegramApi[F],
//      geocoder: GeocoderApi[F],
//  ): F[Unit] =
//    message match {
//      case "/start" =>
//        for {
//          _ <- telegram.sendMessage(startMessage, chat)
//        } yield ()
//
//      case s"//${s: String}" => HtmlHandler().stringToHtml(telegram, geocoder, storage, chat, s)
//
//      case s"/set_line_del ${d: String}" =>
//        if (regexLine.matches(d))
//          setSettingsAndSendMessage(
//            storage,
//            telegram,
//            chat,
//            UserSettings(chat.id, lineDelimiter = Some(d)),
//          )
//        else telegram.sendMessage(incorrectDelimiter, chat)
//
//      case s"/set_del_in_row ${d: String}" =>
//        if (regexInLine.matches(d))
//          setSettingsAndSendMessage(
//            storage,
//            telegram,
//            chat,
//            UserSettings(chat.id, inRowDelimiter = Some(d)),
//          )
//        else telegram.sendMessage(incorrectDelimiter, chat)
//
//      case s"/set_data_model ${model: String}" =>
//        parseDataModel(model) match {
//          case Some(optList) =>
//            optList match {
//              case name :: address :: info :: Nil =>
//                setSettingsAndSendMessage(
//                  storage,
//                  telegram,
//                  chat,
//                  UserSettings(chat.id, nameCol = name, addrCol = address, infoCol = info),
//                )
//              case name :: address :: Nil =>
//                setSettingsAndSendMessage(
//                  storage,
//                  telegram,
//                  chat,
//                  UserSettings(chat.id, nameCol = name, addrCol = address),
//                )
//              case _ => telegram.sendMessage("Error while parsing data model", chat)
//            }
//
//          case None =>
//            telegram.sendMessage(
//              "Incorrect data model! More Information /set_data_model_desc",
//              chat,
//            )
//        }
//
//      case "/settings" =>
//        for {
//          settings <- storage.getSettings(chat.id)
//          _ <- settings match {
//            case Left(_) =>
//              for {
//                create <- storage.createUserSettings(defaultUserSettings.copy(chatId = chat.id))
//                _ <- create match {
//                  case Left(err) => telegram.sendMessage(err.message, chat)
//                  case Right(s)  => telegram.sendMessage(s.message, chat)
//                }
//              } yield ()
//            case Right(settings) => telegram.sendMessage(settings.message, chat)
//          }
//        } yield ()
//
//      case "/set_line_del_desc" => telegram.sendMessage(setLineDelDescription, chat)
//
//      case "/set_del_in_row_desc" => telegram.sendMessage(setDelInRowDescription, chat)
//
//      case "/set_data_model_desc" => telegram.sendMessage(setDataModelDescription, chat)
//
//      case s"/set_line_del" =>
//        telegram.sendMessage("This command must have the parameter. More information /set_line_del_desc", chat)
//
//      case s"/set_del_in_row" =>
//        telegram.sendMessage("This command must have the parameter. More information /set_del_in_row_desc", chat)
//
//      case "/help" => telegram.sendMessage(helpResponse, chat)
//      case _       => telegram.sendMessage(s"Unknown command '$message'", chat)
//    }
//
//  private def setSettingsAndSendMessage[F[_]: Sync](
//      storage: Storage[F],
//      telegram: TelegramApi[F],
//      chat: Chat,
//      us: UserSettings,
//  ) = {
//    for {
//      settings <- storage.setUserSettings(us)
//      _ <- settings match {
//        case Left(_) =>
//          for {
//            create <- storage.createUserSettings(us)
//            _ <- create match {
//              case Left(err) => telegram.sendMessage(err.message, chat)
//              case Right(_)  => telegram.sendMessage("Settings updated successfully! See current /settings", chat)
//            }
//          } yield ()
//        case Right(_) => telegram.sendMessage("Settings updated successfully! See current /settings", chat)
//      }
//    } yield ()
//  }
//  //    (for {
////      settings <- storage.setUserSettings(us)
////      result   <- if (settings.isLeft) storage.createUserSettings(us) else Sync[F].pure(Right(()))
////    } yield result).flatMap {
////      case Left(err) => telegram.sendMessage(err.message, chat)
////      case Right(_)  => telegram.sendMessage("Settings updated successfully! See current /settings", chat)
////    }
//
//  private def parseDataModel(model: String): Option[List[Option[Int]]] = {
//    val sList = model.split("\\s+").toList
//    if (sList.length != sList.distinct.length | sList.length < 2 | sList.length > 3) None
//    else {
//      val optList = sList.map(x => Try[Int](x.toInt).toOption)
//      if (optList.contains(None)) None
//      else Some(optList)
//    }
//  }
//
//  private val helpResponse =
//    """Available commands:
//      |   /start - Initialize bot. If this is your first time
//      |   /settings - Settings of your bot
//      |   set_data_model - configure position of data in your file. More Information /set_data_model_desc
//      |   set_line_del <new_delimiter> - Set new delimiter in row. More information /set_line_del_desc
//      |   set_del_in_row <new_delimiter> - Set new delimiter in row. More information /set_del_in_row_desc
//      |   //<text message with data for creating map>
//      |   /help - Available commands
//      |   """.stripMargin
//
//  private val setLineDelDescription =
//    """It's command for change line delimiter.
//      |Use ONE symbol, no digit or letter.
//      |If you want to use end of line as delimiter, use "nl".
//      |   Examples:
//      |/set_line_del nl
//      |/set_line_del ;""".stripMargin
//
//  private val setDelInRowDescription =
//    """It's command for change delimiter between fields in the line.
//      |Use ONE symbol, no digit or letter.
//      |   Examples:
//      |/set_del_in_row '
//      |/set_del_in_row :""".stripMargin
//
//  private val setDataModelDescription =
//    """It's command for set positions of data in your file.
//      |   Command syntax:
//      |/set_data_model <name(Int)> <address(Int)> <info(Int)(Optional)>
//      |where <name> and <address> - required fields with numbers of columns, which contain corresponding data
//      |<info> - optional field, with number of column, which contain additional info about point
//      |   Examples:
//      |/set_data_model 1 2
//      |/set_data_model 2 1 4
//      |""".stripMargin
//
//  private val startMessage =
//    """Hello! I'm bot and I can:
//      |   1) create map from file
//      |   2) create map from text message
//      |
//      |For successful work:
//      |1) the file must to have simple text extension (".txt", ".csv", etc)
//      |2) the file (or text message) have to contain 2 required fields:
//      |   - address of point (by default, column 1)
//      |   - name of point (by default, column 2)
//      |and 1 optional field:
//      |   - info about point (by default, not defined)
//      | (How to change these settings see /set_data_model_desc)
//      |
//      |3) The following delimiters have to use between file (or text message) fields:
//      |   - between lines (by default, '\n' (new line))
//      |   - between fields in one line (by default, ';')
//      | (How to change these settings see /set_line_del and /set_del_in_row_desc)
//      |
//      |Stages of interactions with me:
//      |   1) Make sure your file meets the above requirements. If no:
//      |       a) edit your file     OR      b) edit my settings
//      |   2) Send me your file
//      |   3) Get the link to the map
//      |   4) It's all! You're beautiful!
//      |
//      |Information about available commands on /help
//      |
//      |Good luck and have fun!""".stripMargin
//}
