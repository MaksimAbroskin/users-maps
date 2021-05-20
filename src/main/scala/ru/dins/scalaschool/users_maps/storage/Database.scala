package ru.dins.scalaschool.users_maps.storage

import cats.effect.{ContextShift, IO}
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux

import scala.concurrent.ExecutionContext
import ru.dins.scalaschool.users_maps.Config.Postgres._

object Database {
  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  val xa: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    s"jdbc:postgresql://$host:$port/$name",
    user,
    pass,
  )
}
