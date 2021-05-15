import sbt._

object Deps {
  object Versions {
    val cats      = "2.3.0"
    val http4s    = "0.21.21"
    val circe     = "0.12.3"
    val fs2       = "2.5.0"
    val doobie    = "0.12.1"
    val logback   = "1.2.3"
    val scalatest = "3.2.7"
    val scalamock = "5.1.0"
  }

  val cats: Seq[ModuleID] = Seq(
    "org.typelevel" %% "cats-effect",
    "org.typelevel" %% "cats-core",
  ).map(_ % Versions.cats)

  val http4s: Seq[ModuleID] = Seq(
    "org.http4s" %% "http4s-dsl",
    "org.http4s" %% "http4s-blaze-server",
    "org.http4s" %% "http4s-blaze-client",
    "org.http4s" %% "http4s-circe",
  ).map(_ % Versions.http4s)

  val circe: Seq[ModuleID] = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser",
    "io.circe" %% "circe-literal",
  ).map(_ % Versions.circe)

  val fs2: Seq[ModuleID] = Seq(
    "co.fs2" %% "fs2-core",
    "co.fs2" %% "fs2-io",
  ).map(_ % Versions.fs2)

  val doobie: Seq[ModuleID] = Seq(
    "org.tpolecat" %% "doobie-core",
    "org.tpolecat" %% "doobie-postgres",
  ).map(_ % Versions.doobie)

  val logback = "ch.qos.logback" % "logback-classic" % Versions.logback

  val tests: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest",
    "org.scalactic" %% "scalactic",
  ).map(_ % Versions.scalatest)

  val mock: Seq[ModuleID] = Seq("org.scalamock" %% "scalamock")
    .map(_ % Versions.scalamock)

}
