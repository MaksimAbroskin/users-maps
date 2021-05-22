package ru.dins.scalaschool.users_maps

object Config {

  object Postgres {
    lazy val postgres = "postgres"

    lazy val host: String = sys.env.getOrElse("DB_HOST", postgres)
    lazy val port: String = sys.env.getOrElse("DB_PORT", "5432")
    lazy val name: String = sys.env.getOrElse("DB_NAME", postgres)
    lazy val user: String = sys.env.getOrElse("DB_USER", postgres)
    lazy val pass: String = sys.env.getOrElse("DB_PASS", postgres)
  }

  object Http {
    lazy val localHost = "0.0.0.0"
    lazy val host: String =
      sys.env
        .get("APPNAME")
        .filterNot(_.isEmpty)
        .map(appName => s"scala-school-final.dins.ru/$appName")
        .getOrElse(s"$localHost:$port")
    lazy val port: Int = 8080
  }
}
