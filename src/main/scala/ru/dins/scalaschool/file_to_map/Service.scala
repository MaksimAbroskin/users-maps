package ru.dins.scalaschool.file_to_map

import cats.effect.Sync
import telegram.TelegramApi
import telegram.model.Offset
import maps.GeocoderApi

object Service {
  def start[F[_]: Sync](telegram: TelegramApi[F], geocoder: GeocoderApi[F]): F[Unit] = {

    val router: Router[F] = Router.apply[F](telegram, geocoder)

    telegram
      .getUpdates(Offset.zero)
      .evalMap(router.handle)
      .compile
      .lastOrError
  }
}
