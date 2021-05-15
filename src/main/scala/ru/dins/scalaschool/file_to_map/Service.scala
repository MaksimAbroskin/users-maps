package ru.dins.scalaschool.file_to_map

import cats.effect.{ContextShift, Sync}
import telegram.TelegramApi
import telegram.model.Offset
import maps.GeocoderApi
import ru.dins.scalaschool.file_to_map.storage.Storage

object Service {
  def start[F[_]: Sync : ContextShift](telegram: TelegramApi[F], geocoder: GeocoderApi[F], storage: Storage[F]): F[Unit] = {

    val router: Router[F] = Router.apply[F](telegram, geocoder, storage)

    telegram
      .getUpdates(Offset.zero)
      .evalMap(router.handle)
      .compile
      .lastOrError
  }
}
