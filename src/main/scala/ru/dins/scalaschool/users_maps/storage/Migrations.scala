package ru.dins.scalaschool.users_maps.storage

import cats.effect.Sync
import doobie.implicits._
import doobie._
import cats.implicits._
import doobie.util.transactor.Transactor.Aux

object Migrations {
  private val migration: Update0 =
    sql"""
      CREATE TABLE IF NOT EXISTS users_settings(
        chat_id NUMERIC NOT NULL UNIQUE,
        line_delimiter VARCHAR NOT NULL,
        in_row_delimiter VARCHAR NOT NULL,
        name_col NUMERIC,
        addr_col NUMERIC NOT NULL,
        info_col NUMERIC,
        PRIMARY KEY (chat_id)
      );
       """.update

  def migrate[F[_]: Sync](xa: Aux[F, Unit]): F[Unit] = migration.run.void.transact(xa)
}
