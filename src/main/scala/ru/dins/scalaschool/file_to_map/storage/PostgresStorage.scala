package ru.dins.scalaschool.file_to_map.storage

import cats.effect.Sync
import cats.implicits._
import doobie.implicits._
import doobie.postgres.sqlstate
import doobie.util.fragments.setOpt
import doobie.util.transactor.Transactor.Aux
import ru.dins.scalaschool.file_to_map.Models.{DatabaseError, ErrorMessage, SimpleError, UserSettings}

case class PostgresStorage[F[_]: Sync](xa: Aux[F, Unit]) extends Storage[F]() {

  override def createUserSettings(chatId: Long, lineDelimiter: String, inRowDelimiter: String): F[Either[ErrorMessage, UserSettings]] =
    sql"INSERT INTO users_settings VALUES ($chatId, $lineDelimiter, $inRowDelimiter, '1', '2')".update
      .withUniqueGeneratedKeys[UserSettings]("chat_id", "line_delimiter", "in_row_delimiter", "name_col", "addr_col", "info_col", "last_file_id")
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

  override def setLineDelimiter(chatId: Long, newDelimiter: String): F[Either[ErrorMessage, UserSettings]] =
    sql"""UPDATE users_settings SET line_delimiter = $newDelimiter WHERE chat_id=$chatId RETURNING *"""
      .query[UserSettings]
      .option
      .transact(xa)
      .map {
        case Some(x) => Right(x)
        case None    => Left(DatabaseError("Couldn't update line delimiter. We are trying to use default delimiters..."))
      }

  override def setInRowDelimiter(chatId: Long, newDelimiter: String): F[Either[ErrorMessage, UserSettings]] =
    sql"""UPDATE users_settings SET in_row_delimiter = $newDelimiter WHERE chat_id=$chatId RETURNING *"""
      .query[UserSettings]
      .option
      .transact(xa)
      .map {
        case Some(x) => Right(x)
        case None    => Left(DatabaseError("Couldn't update delimiter in rows. Please, try again later or use default delimiters"))
      }

  override def setLastFileId(chatId: Long, newFileId: String): F[Either[ErrorMessage, UserSettings]] =
    sql"""UPDATE users_settings SET last_file_id = $newFileId WHERE chat_id=$chatId RETURNING *"""
      .query[UserSettings]
      .option
      .transact(xa)
      .map {
        case Some(x) => Right(x)
        case None    => Left(DatabaseError("Couldn't update file id."))
      }

  def setDataModel(chatId: Long, nameCol: Option[Int], addrCol: Option[Int], infoCol: Option[Int]): F[Either[ErrorMessage, UserSettings]] = {
    val nameOpt = Some(fr"name_col = $nameCol")
    val addrOpt = Some(fr"addr_col = $addrCol")
    val infoOpt = infoCol.map(x => fr"info_col = $x")

    val request = fr"UPDATE users_settings" ++ setOpt(nameOpt, addrOpt, infoOpt) ++ fr"WHERE chat_id=$chatId RETURNING *"
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
