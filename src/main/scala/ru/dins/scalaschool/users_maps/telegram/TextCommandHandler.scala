package ru.dins.scalaschool.users_maps.telegram

import cats.effect.{ContextShift, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.applicativeError._
import ru.dins.scalaschool.users_maps.Models.UserSettings
import ru.dins.scalaschool.users_maps.maps.GeocoderApi
import ru.dins.scalaschool.users_maps.maps.yandex.HtmlHandler
import ru.dins.scalaschool.users_maps.storage.Storage
import ru.dins.scalaschool.users_maps.telegram.model.Chat
import ru.dins.scalaschool.users_maps.{
  charAsString,
  defaultInRowDelimiter,
  defaultLineDelimiter,
  defaultUserSettings,
  leftPart,
  rightPart,
}

import scala.util.Try

object TextCommandHandler {

  private def incorrectDelimiter(link: String) =
    s"""Указан некорректный разделитель! Подробнее $link""".stripMargin

  def handle[F[_]: Sync: ContextShift](
      chat: Chat,
      message: String,
      storage: Storage[F],
      telegram: TelegramApi[F],
      geocoder: GeocoderApi[F],
  ): F[Unit] =
    message match {
      case "/start" =>
        for {
          _ <- telegram.sendMessage(startMessage, chat)
        } yield ()

      case s"//${s: String}" =>
        HtmlHandler()
          .stringToHtml(telegram, geocoder, storage, chat, s)
          .handleErrorWith(_ => Sync[F].delay(println("File didn't create 3")))

      case s"/set_line_del ${d: String}" =>
        getSeparator(d) match {
          case Some(ch) =>
            setSettingsAndSendMessage(
              storage,
              telegram,
              chat,
              UserSettings(chat.id, lineDelimiter = Some(ch)),
            )
          case None => telegram.sendMessage(incorrectDelimiter("/set_line_del_desc"), chat)
        }

      case s"/set_del_in_row ${d: String}" =>
        getSeparator(d) match {
          case Some(ch) =>
            setSettingsAndSendMessage(
              storage,
              telegram,
              chat,
              UserSettings(chat.id, inRowDelimiter = Some(ch)),
            )
          case None => telegram.sendMessage(incorrectDelimiter("/set_del_in_row_desc"), chat)
        }

      case s"/set_data_model ${model: String}" =>
        parseDataModel(model) match {
          case Some(optList) =>
            optList match {
              case name :: address :: info :: Nil =>
                setSettingsAndSendMessage(
                  storage,
                  telegram,
                  chat,
                  UserSettings(chat.id, nameCol = name, addrCol = address, infoCol = info),
                )
              case name :: address :: Nil =>
                setSettingsAndSendMessage(
                  storage,
                  telegram,
                  chat,
                  UserSettings(chat.id, nameCol = name, addrCol = address),
                )
              case _ =>
                telegram.sendMessage("Структура данных указана некорректно! Подробнее /set_data_model_desc", chat)
            }

          case None =>
            telegram.sendMessage(
              "Структура данных указана некорректно! Подробнее /set_data_model_desc",
              chat,
            )
        }

      case s"/set_single_data_model ${model: String}" =>
        if (model != "L" & model != "R")
          telegram.sendMessage(
            "Неверно указан параметр! Может принимать значение только 'L' или 'R'. Подробнее /set_data_model_desc",
            chat,
          )
        else {
          setSettingsAndSendMessage(
            storage,
            telegram,
            chat,
            UserSettings(chat.id, addrCol = if (model == "L") Some(leftPart) else Some(rightPart)),
          )
        }

      case "/settings" =>
        for {
          settings <- storage.getSettings(chat.id)
          _ <- settings match {
            case Left(_) =>
              for {
                create <- storage.createUserSettings(defaultUserSettings.copy(chatId = chat.id))
                _ <- create match {
                  case Left(err) => telegram.sendMessage(err.message, chat)
                  case Right(s)  => telegram.sendMessage(s.message, chat)
                }
              } yield ()
            case Right(settings) => telegram.sendMessage(settings.message, chat)
          }
        } yield ()

      case "/set_line_del_desc" => telegram.sendMessage(setLineDelDescription, chat)

      case "/set_del_in_row_desc" => telegram.sendMessage(setDelInRowDescription, chat)

      case "/set_data_model_desc" => telegram.sendMessage(setDataModelDescription, chat)

      case "/parse_text_desc" => telegram.sendMessage(parseTextDescription, chat)

      case s"/set_line_del" =>
        telegram.sendMessage("Эта команда должна использоваться с параметром. Подробнее /set_line_del_desc", chat)

      case s"/set_del_in_row" =>
        telegram.sendMessage("Эта команда должна использоваться с параметром. Подробнее /set_del_in_row_desc", chat)

      case "/help" => telegram.sendMessage(helpResponse, chat)
      case _       => telegram.sendMessage(s"Неизвестная команда '$message'", chat)
    }

  def getSeparator(in: String): Option[Char] =
    if (in.startsWith("\\") && in.length == 2)
      in match {
        case """\t""" => Some('\t')
        case """\n""" => Some('\n')
        case _        => None
      }
    else if (in.length == 1 && !in.head.isLetterOrDigit)
      Some(in.charAt(0))
    else
      None

  private def setSettingsAndSendMessage[F[_]: Sync](
      storage: Storage[F],
      telegram: TelegramApi[F],
      chat: Chat,
      us: UserSettings,
  ) =
    storage.getSettings(chat.id).flatMap {
      case Right(_) =>
        for {
          _ <- storage.setUserSettings(us)
          _ <- telegram.sendMessage("Параметры обновлены! Текущие настройки - /settings", chat)
        } yield ()
      case Left(_) =>
        for {
          create <- storage.createUserSettings(defaultUserSettings)
          _ <- create match {
            case Right(_) =>
              storage
                .setUserSettings(us)
                .flatMap {
                  case Right(_)  => telegram.sendMessage("Параметры обновлены! Текущие настройки - /settings", chat)
                  case Left(err) => telegram.sendMessage(err.message, chat)
                }
            case Left(err) => telegram.sendMessage(err.message, chat) // should never happen
          }
        } yield ()
    }

  private def parseDataModel(model: String): Option[List[Option[Int]]] = {
    val sList    = model.split("\\s+").toList
    val distList = sList.filter(_.matches("[1-9]")).distinct
    if (sList.length != distList.length | sList.length < 2 | sList.length > 3) None
    else {
      val optList = sList.map(x => Try[Int](x.toInt).toOption)
      if (optList.contains(None)) None
      else Some(optList)
    }
  }

  private val helpResponse =
    """Доступные команды:
      |   /start - Инструкция к боту
      |   /settings - Настройки бота
      |   /set_data_model_desc - Инструкция: как сменить модели данных
      |   /set_line_del_desc - Инструкция: как задать межстрочный разделитель
      |   /set_line_del_desc - Инструкция, как задать разделитель между полями одной записи
      |   /parse_text_desc - Инструкция, как разобрать данные прямо из текстового сообщения, а не из файла
      |   /help - Перечень доступных команд""".stripMargin

  private val parseTextDescription =
    """Команда для разбора данных прямо из текстового сообщения.
      |Отправьте в виде сообщения данные, отформатированные в
      |соответствии с настройками (/settings) и получите в ответ карту.
      |Синтаксис: //<данные для разбора>
      |
      |   Примеры:
      |//Название какой-то организации, Санкт-Петербург, Коломяжский пр. дом 27
      |Название еще какой-то организации, пр. Большевиков, д. 25
      |Ещё одно название организации, Ветеранов пр. 108""".stripMargin

  private val setLineDelDescription =
    """   Задание разделителя между записями.
      |В качестве разделителя может использоваться любой одиночный символ, кроме букв, цифр и символов '(' и '\'.
      |Чтобы использовать в качестве разделителя перенос строки, используйте '\n'
      |Синтаксис: /set_line_del <новый разделитель>
      |
      |   Примеры:
      |/set_line_del \n
      |/set_line_del ;""".stripMargin

  private val setDelInRowDescription =
    """   Задание разделителя между полями одной записи.
      |В качестве разделителя может использоваться любой одиночный символ, кроме букв, цифр и символов '(' и '\'.
      |Чтобы использовать в качестве разделителя табуляцию, используйте '\t'.
      |ВАЖНО: использование в качестве разделителя '\t' возможно только при разборе
      |данных из файла! Текстовое сообщение будет разобрано некорректно!
      |Синтаксис: /set_del_in_row <новый разделитель>
      |
      |   Примеры:
      |/set_del_in_row \t
      |/set_del_in_row :""".stripMargin

  private val setDataModelDescription =
    """Задание структуры данных в Вашем файле или передаваемом тексте
      |   По умолчанию используется data_model 1 2
      |           | название | адрес |
      |
      |   Возможны 2 варианта синтаксиса команды:
      |   1) Хорошо структурированные данные:
      |Синтаксис: /set_data_model <N> <A> <I(Option)>
      |Здесь параметры - натуральные числа от 1 до 9.
      |N - Номер столбца с названием объекта
      |A - Номер столбца с адресом объекта
      |I - Номер столбца с дополнительной информацией об объекте (предоставление этого поля не обязательно)
      |
      |   Примеры:
      |/set_data_model 1 2 - указывает, что Ваши данные имеют вид
      |   | название | адрес |
      |
      |/set_data_model 2 1 4
      |   | адрес | название | дополнительная информация |
      |
      |
      |   2) Слабо структурированные данные:
      |set_single_data_model <параметр>
      |
      |Здесь в качестве параметра необходимо указать, в какой части каждой записи находится адрес организации.
      |Возможные значения
      | - 'L' - адрес в левой части
      | - 'R' - адрес в правой части
      |
      |   Примеры:
      |/set_single_data_model L - для данных вида
      |Санкт-Петербург, Невский пр. 30, Контора "Рога и копыта"
      |
      |   Примеры:
      |/set_single_data_model R - для данных вида
      |Контора "Рога и копыта", находящаяся по адресу Невский пр., дом 30
      |
      |ВАЖНО: Для слабоструктурированных данных вероятность не найти часть объектов выше, чем для хорошо структурированных!
      |""".stripMargin

  private val startMessage =
    s"""   Привет! Я бот и я могу:
      |1) Создавать карту из файла с адресами
      |2) Создавать карту из текстового сообщения с адресами
      |
      |Чтобы я хорошо делал свою работу, необходимо:
      |1) Рассказать мне о структуре Ваших данных /set_data_model_desc
      |   По умолчанию используется data_model 1 2
      |           | название | адрес |
      |
      |2) Настроить разделители данных.
      |   По умолчанию используется:
      |${charAsString(defaultLineDelimiter)} (перенос строки) - между записями /set_line_del_desc
      |${charAsString(defaultInRowDelimiter)} - между полями одной записи /set_del_in_row_desc
      |
      |
      |Этапы работы с ботом:
      |   1) Убедитесь, что Ваши данные (файл или текст) соответствуют
      |   приведенным выше требованиям. Если нет, то:
      |       а) отредактируйте данные
      |           ИЛИ
      |       б) отредактируйте настройки бота
      |
      |   2) Передайте данные боту (отправьте файл или сообщение)
      |
      |   3) Получите ссылку на карту
      |
      |   4) Готово! Вы прекрасны!
      |
      |Информация о доступных командах /help
      |
      |Приятной работы!""".stripMargin
}
