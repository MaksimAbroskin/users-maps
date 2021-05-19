package for_tests

import cats.effect.{Blocker, ExitCode, IO, IOApp}
import io.circe.syntax.EncoderOps
import fs2._
import org.apache.commons.text.StringEscapeUtils
import ru.dins.scalaschool.users_maps.Models.Note
import ru.dins.scalaschool.users_maps.maps.yandex.YaPointToMap.{YaData, YaOneFeature}

import java.nio.file.{Files, Paths}
import scala.reflect.runtime.universe.{Constant, Literal}
import org.apache.commons.text.StringEscapeUtils._

import scala.StringContext.{processEscapes, treatEscapes}

  object Main extends App {
    val s = """"\This string\\ \contains some\ backslashes\""""
    println(s)
    val rx = """(\\)\\(?!\\)""".r.unanchored
    val ismatch = s match {
      case rx(_*) => true
      case _ => false
    }
    println(ismatch)
  }

