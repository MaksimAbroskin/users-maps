package ru.dins.scalaschool.users_maps

import cats.effect.{Blocker, ContextShift, Sync}
import fs2.{Stream, io, text}

import java.nio.file.{Path, Paths, StandardOpenOption}

object Utils {

  def copyFile[F[_]: Sync: ContextShift](fromPath: String, toPath: String)(implicit blocker: Blocker): F[Unit] =
    io.file
      .readAll[F](Paths.get(fromPath), blocker, 4096)
      .through(
        io.file.writeAll(
          Paths.get(toPath),
          blocker,
          List(StandardOpenOption.CREATE),
        ),
      )
      .compile
      .drain

  def createFile[F[_]: Sync: ContextShift](
      path: String,
      upstream: Stream[F, String],
  ): F[Unit] =
    (for {
      blocker <- Stream.resource(Blocker[F])
      _ <- upstream
        .through(text.utf8Encode)
        .through(io.file.writeAll(Paths.get(s"$path"), blocker))
    } yield ()).compile.drain

  def createDirectory[F[_]: Sync: ContextShift](name: String): F[Path] =
    Blocker[F].use { blocker =>
      io.file.createDirectories(blocker, Paths.get(name))
    }
}
