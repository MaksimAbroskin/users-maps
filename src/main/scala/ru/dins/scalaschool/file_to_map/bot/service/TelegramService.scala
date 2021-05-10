package ru.dins.scalaschool.file_to_map.bot.service

import cats.effect.{ContextShift, Sync}
import org.http4s.client.Client
import ru.dins.scalaschool.file_to_map.bot.api.TelegramApi
import ru.dins.scalaschool.file_to_map.bot.api.model.Offset
import ru.dins.scalaschool.file_to_map.maps.Geocoder

object TelegramService {
  def start[F[_]: Sync :ContextShift](telegram: TelegramApi[F], geocoder: Geocoder[F], client: Client[F]): F[Unit] = {
    val router: Router[F] = Router.ofTelegramApi[F](telegram, geocoder, client)

    telegram
      .getUpdates(Offset.zero)
      .evalMap(router.handle)
      .compile
      .lastOrError
  }
}
