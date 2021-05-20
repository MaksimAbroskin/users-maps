package ru.dins.scalaschool.users_maps

object Config {

  object Postgres {
    lazy val postgres = "postgres"

    lazy val host: String = sys.env.getOrElse("DB_HOST", postgres)
    lazy val port: String = sys.env.getOrElse("DB_HOST", "5432")
    lazy val name: String = sys.env.getOrElse("DB_NAME", postgres)
    lazy val user: String = sys.env.getOrElse("DB_NAME", postgres)
    lazy val pass: String = sys.env.getOrElse("DB_NAME", postgres)
  }
}