package ru.dins.scalaschool.users_maps.storage

import cats.effect.{ContextShift, IO}
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux

import scala.concurrent.ExecutionContext

object Database {
  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  val xa: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql:postgres",
    "postgres",
    "postgres",
  )
}
