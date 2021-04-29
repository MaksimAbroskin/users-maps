package ru.dins.scalaschool.file_to_map.bot.service

import cats.effect.Sync
import ru.dins.scalaschool.file_to_map.bot.api.TelegramApi
import ru.dins.scalaschool.file_to_map.bot.api.model.Offset

object TelegramService {
  def start[F[_]: Sync](telegram: TelegramApi[F]): F[Unit] = {
    val router: Router[F] = Router.ofTelegramApi[F](telegram)

    telegram
      .getUpdates(Offset.zero)
      .evalMap(router.handle)
      .compile
      .lastOrError
  }
}
