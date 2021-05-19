package ru.dins.scalaschool.users_maps.storage

import cats.effect.{ContextShift, IO}
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import org.scalatest.Assertion
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import ru.dins.scalaschool.users_maps.defaultUserSettings
import ru.dins.scalaschool.users_maps.Models.{ChatAlreadyExistsError, ChatNotFoundInDbError, UserSettings}

import scala.concurrent.ExecutionContext

class PostgresStorageTest extends AnyFlatSpec with Matchers with TestContainerForAll {
  implicit val cs: ContextShift[IO]                  = IO.contextShift(ExecutionContext.global)
  override val containerDef: PostgreSQLContainer.Def = PostgreSQLContainer.Def()

  private def createTransactor(container: Containers): Aux[IO, Unit] =
    Transactor.fromDriverManager[IO](
      driver = "org.postgresql.Driver",
      url = container.jdbcUrl,
      user = container.username,
      pass = container.password,
    )

  override def startContainers(): PostgreSQLContainer = {
    val container = super.startContainers()
    val xa        = createTransactor(container)
    Migrations.migrate(xa).unsafeRunSync()
    container
  }

  // Чистим таблицу перед каждым тестом
  private def resetStorage(test: (Storage[IO], Transactor.Aux[IO, Unit]) => IO[Assertion]): Unit =
    withContainers { container =>
      val xa       = createTransactor(container)
      val truncate = sql"truncate users_settings".update.run
      val storage  = PostgresStorage(xa)

      val result = for {
        _         <- truncate.transact(xa)
        assertion <- test(storage, xa)
      } yield assertion

      result.unsafeRunSync()
    }

  private val us: UserSettings = defaultUserSettings

  // ----------------------------------------------------------------------------------
  // ---------------------- createUserSettings(us: UserSettings) ----------------------
  // ----------------------------------------------------------------------------------

  "createUserSettings" should "return userSettings if success" in resetStorage { case (storage, _) =>
    storage
      .createUserSettings(defaultUserSettings)
      .map(_ shouldBe Right(defaultUserSettings))
  }

  it should "return error in storage exist chat with same id" in resetStorage { case (storage, _) =>
    for {
      _      <- storage.createUserSettings(defaultUserSettings)
      result <- storage.createUserSettings(defaultUserSettings)
    } yield result shouldBe Left(ChatAlreadyExistsError(defaultUserSettings.chatId))
  }

  // ----------------------------------------------------------------------------------
  // ---------------------------- getSettings(chatId: Long) ---------------------------
  // ----------------------------------------------------------------------------------

  "getSettings" should "return userSettings if it's exists" in resetStorage { case (storage, xa) =>
    val createNoteForChat =
      sql"INSERT INTO users_settings VALUES (${us.chatId}, ${us.lineDelimiter}, ${us.inRowDelimiter}, ${us.nameCol}, ${us.addrCol})".update.run
        .transact(xa)

    for {
      _ <- createNoteForChat
      settings <- storage.getSettings(defaultUserSettings.chatId)
    } yield settings shouldBe Right(defaultUserSettings)
  }

  it should "return error if it's not exist" in resetStorage { case (storage, _) =>
    for {
      settings <- storage.getSettings(defaultUserSettings.chatId)
    } yield settings shouldBe Left(ChatNotFoundInDbError(defaultUserSettings.chatId))
  }

  // ----------------------------------------------------------------------------------еуч
  // ----------------------- setUserSettings(us: UserSettings) ------------------------
  // ----------------------------------------------------------------------------------
  "setUserSettings" should "return error if it's not exist" in resetStorage { case (storage, _) =>
    for {
      settings <- storage.setUserSettings(defaultUserSettings)
    } yield settings shouldBe Left(ChatNotFoundInDbError(defaultUserSettings.chatId))
  }

  "set delimiters" should "return edited userSettings if success" in resetStorage { case (storage, xa) =>
    val createNoteForChat =
      sql"INSERT INTO users_settings VALUES (${us.chatId}, ${us.lineDelimiter}, ${us.inRowDelimiter}, ${us.nameCol}, ${us.addrCol})".update.run
        .transact(xa)

    val newUserSettings = UserSettings(chatId = us.chatId, lineDelimiter = Some("-"), inRowDelimiter = Some("'"))

    for {
      _ <- createNoteForChat
      settings <- storage.setUserSettings(newUserSettings)
    } yield settings shouldBe Right(us.copy(lineDelimiter = Some("-"), inRowDelimiter = Some("'")))
  }

  "set data model" should "return edited userSettings if success" in resetStorage { case (storage, xa) =>
    val createNoteForChat =
      sql"INSERT INTO users_settings VALUES (${us.chatId}, ${us.lineDelimiter}, ${us.inRowDelimiter}, ${us.nameCol}, ${us.addrCol})".update.run
        .transact(xa)

    val newUserSettings = UserSettings(chatId = us.chatId, nameCol = Some(4), addrCol = Some(3), infoCol = Some(1))

    for {
      _ <- createNoteForChat
      settings <- storage.setUserSettings(newUserSettings)
    } yield settings shouldBe Right(us.copy(nameCol = Some(4), addrCol = Some(3), infoCol = Some(1)))
  }

}
