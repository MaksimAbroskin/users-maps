package for_tests

import cats.effect.{Blocker, ExitCode, IO, IOApp}
import io.circe.syntax.EncoderOps
import fs2._
import ru.dins.scalaschool.file_to_map.Models.Note
import ru.dins.scalaschool.file_to_map.maps.yandex.YaPointToMap.{YaData, YaOneFeature}

import java.nio.file.Paths

object Main extends IOApp {

//  val mockUpstream: Stream[IO, Int]     = Stream.iterate(0)(_ + 1).take(100).covary[IO]
//  val s                                 = "hahaha"
//  val mockUpstream2: Stream[IO, String] = Stream(s)
//  val mockUpstream3: Stream[IO, String] = Stream(" gigigi")
//
//  val data = YaData(features = List(YaOneFeature(Note(name = "name", address =  "addr"))))
//  val mockData: Stream[IO, String] = Stream(data.asJson.toString())

//  def writeToFileExample: IO[Unit] =
//    (for {
//      blocker <- Stream.resource(Blocker[IO])
//      q <- Stream.eval(
//        Queue.bounded[IO, Option[Either[Throwable, String]]](1),
//      )
//      writeToFileInstance <- Stream.eval(
//        WriteToFile.create[IO](q, destinationFile = "src/main/resources/testFile.txt")(
//          blocker,
//          Concurrent[IO],
//          contextShift,
//        ),
//      )
//
//      _ <- mockUpstream
//        //        .through(printStream[Int])
//        .evalMap(i =>
//          writeToFileInstance.write(
//            s"[${Thread.currentThread.getName}] Coming from upstream $i",
//          ),
//        )
//    } yield ()).compile.drain

//  def toFile(fileName: String, upstream: Stream[IO, String], blocker: Blocker): Stream[IO, Unit] =
//    upstream.through(text.utf8Encode).through(io.file.writeAll(Paths.get(fileName), blocker))

  override def run(args: List[String]): IO[ExitCode] = ???
//  {
//    val prog = for {
//      _ <- Stream
//        .resource(Blocker[IO])
//        .flatMap(blocker => toFile("src/main/resources/testFile.txt", mockUpstream2 ++ mockData ++ mockUpstream3, blocker))
//    } yield ()
//    prog.compile.drain.as(ExitCode.Success)
//  }
}
