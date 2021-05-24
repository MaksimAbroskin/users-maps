package ru.dins.scalaschool.users_maps.storage

import org.flywaydb.core.Flyway
import ru.dins.scalaschool.users_maps.Config

object Migrations {
  val flyway: Flyway = Flyway
    .configure()
    .dataSource(
      s"jdbc:postgresql://${Config.Postgres.host}:${Config.Postgres.port}/${Config.Postgres.name}",
      Config.Postgres.user,
      Config.Postgres.pass,
    )
    .locations("db/migration")
    .baselineOnMigrate(true)
    .load()
}
