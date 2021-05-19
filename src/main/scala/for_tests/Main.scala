package for_tests

import cats.effect.{Blocker, ExitCode, IO, IOApp}
import io.circe.syntax.EncoderOps
import fs2._
import ru.dins.scalaschool.users_maps.Models.Note
import ru.dins.scalaschool.users_maps.maps.yandex.YaPointToMap.{YaData, YaOneFeature}

import java.nio.file.{Files, Paths}

object Main extends App {
  def combine(outcomeA: Either[String, Int],
              outcomeB: Either[String, Int]): Either[String, Int] =
    for {
      passA <- outcomeA
      passB <- outcomeB
    } yield {(passA + passB) / 2}

  println(combine(Right(3), Left("error2")))


}
