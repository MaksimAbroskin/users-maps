package ru.dins.scalaschool.file_to_map.fileParse

import cats.effect.{Blocker, ExitCode, IO, IOApp}
import fs2._
import ru.dins.scalaschool.file_to_map.models.Models.Note

import java.nio.file.Paths
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object FileHandle extends IOApp {
  val blockingExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))

  val path          = "C:\\_Scala\\test.csv"
  val lineSeparator = "'"
  val inLineSeparator = ":"

  def stringToNotesList(s: String, lineSeparator: String, inLineSeparator: String): List[Note] = {
    val strNotes = s.split(lineSeparator).toList.map(s => {
      val sNote = s.split(inLineSeparator).toList
      sNote match {
        case name :: addr :: Nil => Some(Note(name, addr))
        case _ => None
      }
    })
    strNotes.filterNot(_.isEmpty).map(x => x.get)
  }

  val parser: Stream[IO, Unit] =
    io.file
      .readAll[IO](Paths.get(path), Blocker.liftExecutionContext(blockingExecutionContext), 4096)
      .through(text.utf8Decode)
      .map(x => stringToNotesList(x, lineSeparator, inLineSeparator))
//      .unNoneTerminate // terminate when done
      .evalMap(x => IO(println(x)))

  val program: IO[Unit] =
    parser.compile.drain.guarantee(IO(blockingExecutionContext.shutdown()))

  override def run(args: List[String]): IO[ExitCode] =
    program.as(ExitCode.Success)

}
