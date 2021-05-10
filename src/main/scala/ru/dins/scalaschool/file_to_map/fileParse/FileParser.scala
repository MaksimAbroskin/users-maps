package ru.dins.scalaschool.file_to_map.fileParse

import cats.Applicative
import cats.effect.{ContextShift, Sync}
import cats.implicits.catsSyntaxApplicativeId
import cats.syntax.functor._
import cats.syntax.flatMap._
import org.http4s.client.Client
import ru.dins.scalaschool.file_to_map.maps.Coordinates
import ru.dins.scalaschool.file_to_map.maps.yandex.YaGeocoder
import ru.dins.scalaschool.file_to_map.models.Models.Note

trait FileParser[F[_]] {
  val strToListNoF: List[Note]
  val stringToNotesList: F[List[Note]]
  def addCoordinatesToNotes(f: String => F[Coordinates]): F[List[Note]]
  def NotesListToOut(): Unit
}

object FileParser {
  def apply[F[_]: Sync: ContextShift](in: String, client: Client[F]): FileParser[F] = new FileParser[F] {

    val lineSeparator   = "'"
    val inLineSeparator = ":"

    override val strToListNoF: List[Note] = in
      .replaceAll(System.lineSeparator(), "")
      .split(lineSeparator)
      .toList
      .map(line =>
        line.split(inLineSeparator).toList match {
          case name :: address :: Nil => Some(Note(name, address))
          case _                      => None
        },
      )
      .filter(_.isDefined)
      .map(note => note.get)

    override val stringToNotesList: F[List[Note]] =
      Sync[F].delay(
        in
          .replaceAll(System.lineSeparator(), "")
          .split(lineSeparator)
          .toList
          .map(line =>
            line.split(inLineSeparator).toList match {
              case name :: address :: Nil => Some(Note(name, address))
              case _                      => None
            },
          )
          .filter(_.isDefined)
          .map(note => note.get),
      )

    override def addCoordinatesToNotes(f: String => F[Coordinates]): F[List[Note]] = ???
//      for {
//        notes <- stringToNotesList.map(note => {
//          for {
//            coord <- YaGeocoder.apply(client).getCoordinates(note.address)
//            note = Note(note.name, note.address, Some(coord))
//          } yield note
//        })
//
////          newN <- notes
//        newNotes <- notes.map(note => {
//          for {
//            coord <- YaGeocoder.apply(client).getCoordinates(note.address)
//            note = Note(note.name, note.address, Some(coord))
//          } yield note
////          val coord1 = YaGeocoder.apply(client).getCoordinates(note.address)
////          println(s"coord1 = $coord1")
////          val coord = f(note.address)
////          println(s"coord = $coord")
////          coordFor
////          Note(note.name, note.address, coordFor)
//        }
////          for {
////            coord <- f(note.address)
////          } yield Note(note.name, note.address, Some(coord)),
//        ).pure
//      } yield newNotes
//      stringToNotesList.map { n =>
//        n.map(note =>
//          for {
//            coord <- f(note.address)
//          } yield Note(note.name, note.address, Some(coord)),
//        )
//      }

    override def NotesListToOut(): Unit = ???
  }
}
