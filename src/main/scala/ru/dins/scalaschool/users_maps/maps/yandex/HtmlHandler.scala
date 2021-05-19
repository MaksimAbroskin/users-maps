package ru.dins.scalaschool.users_maps.maps.yandex

import cats.effect.{Blocker, ContextShift, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import fs2.{Stream, io, text}
import ru.dins.scalaschool.users_maps
import ru.dins.scalaschool.users_maps.Router.createAndSendHtml
import ru.dins.scalaschool.users_maps.maps.GeocoderApi
import ru.dins.scalaschool.users_maps.storage.Storage
import ru.dins.scalaschool.users_maps.telegram.TelegramApi
import ru.dins.scalaschool.users_maps.telegram.model.Chat
import ru.dins.scalaschool.users_maps.StringParser

import java.nio.file.Paths

final case class HtmlHandler[F[_]: Sync: ContextShift]() {
  val prefix: Stream[F, String] = Stream("""<!DOCTYPE html>
                           |<html>
                           |<head>
                           |    <title>User maps</title>
                           |    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
                           |    <!--
                           |        Укажите свой API-ключ. Тестовый ключ НЕ БУДЕТ работать на других сайтах.
                           |        Получить ключ можно в Кабинете разработчика: https://developer.tech.yandex.ru/keys/
                           |    -->
                           |    <script src="https://api-maps.yandex.ru/2.1/?lang=ru-RU&amp;apikey=85e83a9b-10f8-4dd2-98db-47687cb13067" type="text/javascript"></script>
                           |    <script src="https://yandex.st/jquery/2.2.3/jquery.min.js" type="text/javascript"></script>
                           |    <script>
                           |    ymaps.ready(init);
                           |
                           |    function init () {
                           |        var myMap = new ymaps.Map('map', {
                           |                center: [59.943963, 30.225007],
                           |                zoom: 11
                           |            }, {
                           |                searchControlProvider: 'yandex#search'
                           |            }),
                           |            objectManager = new ymaps.ObjectManager({
                           |                // Чтобы метки начали кластеризоваться, выставляем опцию.
                           |                clusterize: true,
                           |                // ObjectManager принимает те же опции, что и кластеризатор.
                           |                gridSize: 32,
                           |                clusterDisableClickZoom: true
                           |            });
                           |
                           |        // Чтобы задать опции одиночным объектам и кластерам,
                           |        // обратимся к дочерним коллекциям ObjectManager.
                           |        objectManager.objects.options.set('preset', 'islands#DotIcon');
                           |        objectManager.clusters.options.set('preset', 'islands#ClusterIcons');
                           |        myMap.geoObjects.add(objectManager);
                           |
                           |        objectManager.add(""".stripMargin)
  val suffix: Stream[F, String] = Stream(""");
                           |
                           |
                           |}
                           |    </script>
                           |	<style>
                           |        html, body, #map {
                           |            width: 100%; height: 100%; padding: 0; margin: 0;
                           |        }
                           |        a {
                           |            color: #04b; /* Цвет ссылки */
                           |            text-decoration: none; /* Убираем подчеркивание у ссылок */
                           |        }
                           |        a:visited {
                           |            color: #04b; /* Цвет посещённой ссылки */
                           |        }
                           |        a:hover {
                           |            color: #f50000; /* Цвет ссылки при наведении на нее курсора мыши */
                           |        }
                           |    </style>
                           |</head>
                           |<body>
                           |<div id="map"></div>
                           |</body>
                           |</html>
                           |""".stripMargin)

  def toFile(fileName: String, upstream: Stream[F, String], blocker: Blocker): Stream[F, Unit] =
    upstream
      .through(text.utf8Encode)
      .through(io.file.writeAll(Paths.get(fileName), blocker))

  def createFile(fileName: String, upstream: Stream[F, String]): F[Unit] = (for {
    _ <- Stream
      .resource(Blocker[F])
      .flatMap(blocker => toFile(fileName, prefix ++ upstream ++ suffix, blocker))
  } yield ()).compile.drain

  def stringToHtml(
      telegram: TelegramApi[F],
      geocoder: GeocoderApi[F],
      storage: Storage[F],
      chat: Chat,
      content: String,
  ): F[Unit] =
    for {
      userSettings <- storage.getSettings(chat.id)
      notes =
        StringParser.parse(
          content,
          userSettings match {
            case Left(err) =>
              telegram.sendMessage(err.message, chat)
              users_maps.defaultUserSettings.copy(chatId = chat.id)
            case Right(userSettings) => userSettings
          },
        )
      _ <- notes match {
        case Left(err) => telegram.sendMessage(err.message, chat)
        case Right(notesWithInfo) =>
          for {
            _             <- telegram.sendMessage(notesWithInfo.info, chat)
            enrichedNotes <- geocoder.enrichNotes(notesWithInfo.notes)
            _ <- enrichedNotes match {
              case Left(err)            => telegram.sendMessage(err.message, chat)
              case Right(notesWithInfo) => createAndSendHtml(telegram, chat, notesWithInfo)
            }
          } yield ()
      }
    } yield ()

}
