package ru.dins.scalaschool.file_to_map.storage

import cats.effect.Sync
import cats.implicits._
import doobie.implicits._
import doobie.postgres.sqlstate
import doobie.util.fragments.setOpt
import doobie.util.transactor.Transactor.Aux
import ru.dins.scalaschool.file_to_map.Models.{DatabaseError, ErrorMessage, SimpleError, UserSettings}

case class PostgresStorage[F[_]: Sync](xa: Aux[F, Unit]) extends Storage[F]() {

  override def createUserSettings(us: UserSettings): F[Either[ErrorMessage, UserSettings]] =
    sql"INSERT INTO users_settings VALUES (${us.chatId}, ${us.lineDelimiter}, ${us.inRowDelimiter}, ${us.nameCol}, ${us.addrCol})".update
      .withUniqueGeneratedKeys[UserSettings](
        "chat_id",
        "line_delimiter",
        "in_row_delimiter",
        "name_col",
        "addr_col",
        "info_col",
        "last_file_id",
      )
      .transact(xa)
      .attemptSomeSqlState { case sqlstate.class23.UNIQUE_VIOLATION =>
        SimpleError("Bot is ready")
      }

  override def getSettings(chatId: Long): F[Either[ErrorMessage, UserSettings]] =
    sql"SELECT * FROM users_settings WHERE chat_id=$chatId"
      .query[UserSettings]
      .option
      .transact(xa)
      .map {
        case Some(x) => Right(x)
        case None    => Left(DatabaseError(s"couldn't take data for chat with id $chatId"))
      }

  override def setUserSettings(us: UserSettings): F[Either[ErrorMessage, UserSettings]] = {
    val lineDelimiterOpt = us.lineDelimiter.map(x => fr"line_delimiter = $x")
    val inRowOpt         = us.inRowDelimiter.map(x => fr"in_row_delimiter = $x")
    val nameOpt          = us.nameCol.map(x => fr"name_col = $x")
    val addrOpt          = us.addrCol.map(x => fr"addr_col = $x")
    val infoOpt =
      if (nameOpt.isDefined & addrOpt.isDefined & us.infoCol.isEmpty) Some(fr"info_col = null")
      else us.infoCol.map(x => fr"info_col = $x")
    val lastFileIdOpt = us.lastFileId.map(x => fr"last_file_id = $x")

    val request = fr"UPDATE users_settings" ++ setOpt(
      lineDelimiterOpt,
      inRowOpt,
      nameOpt,
      addrOpt,
      infoOpt,
      lastFileIdOpt,
    ) ++ fr"WHERE chat_id=${us.chatId} RETURNING *"
    request
      .query[UserSettings]
      .option
      .transact(xa)
      .map {
        case Some(x) => Right(x)
        case None    => Left(DatabaseError("Couldn't update data structure."))
      }
  }

}
