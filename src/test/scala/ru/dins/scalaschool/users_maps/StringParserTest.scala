package ru.dins.scalaschool.users_maps

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import ru.dins.scalaschool.users_maps.Models.{FileParsingError, Note, NotesWithInfo}

class StringParserTest extends AnyFlatSpec with Matchers {
  private val note1 = Note(1, "Name1", "Address1", Some("Info1"))
  private val note2 = Note(2, "Name2", "Address2")
  private val note3 = Note(3, "Name3", "Address3", Some("Info3"))

  "parse" should "return notes list if string is correct" in {
    val s =
      """Name1;Address1;Info1
        |Name2;Address2
        |Name3;Address3;Info3""".stripMargin
    StringParser.parse(s, defaultUserSettings) shouldBe Right(
      NotesWithInfo(List(note1.copy(info = None), note2, note3.copy(info = None)), StringParser.parseNoErrReport(3, 3)),
    )
  }

  it should "return notes list if string have some formatting troubles" in {
    val s =
      """ Name1;Address1 ; Info1'Name2 ;Address2'Name3; Address3;  Info3 '"""
    StringParser.parse(s, defaultUserSettings.copy(lineDelimiter = Some('\''), infoCol = Some(3))) shouldBe Right(
      NotesWithInfo(List(note1, note2, note3), StringParser.parseNoErrReport(3, 3)),
    )
  }

  it should "return parsing error if line delimiter incorrect" in {
    val s =
      """Name1:Address1|Name2:Address2|Name3:Address3|""".stripMargin
    StringParser.parse(s, defaultUserSettings) shouldBe Left(
      FileParsingError("Строка #1: Name1:Address1|Name2:Address2|Name3:Address3|"),
    )
  }

  "this parse" should "return parsing error if in row delimiter incorrect" in {
    val s =
      """Name1,Address1
        |Name2,Address2
        |Name3,Address3""".stripMargin
    StringParser.parse(s, defaultUserSettings) shouldBe Left(FileParsingError("Строка #1: Name1,Address1"))
  }

  it should "return notes list if at least one row parsed successful" in {
    val s =
      """Name1:Address1:Info1
        |Name2;Address2
        |Name3Address3,ERROR""".stripMargin
    StringParser.parse(s, defaultUserSettings.copy(infoCol = Some(3))) shouldBe Right(
      NotesWithInfo(List(note2), StringParser.parseWithErrReport(1, 3, "Строка #1: Name1:Address1:Info1")),
    )
  }

  it should "return error if no successful parsed rows" in {
    val s =
      """Name1:Address1:ERROR
        |Name2'Address2'ERROR
        |Name3-Address3-ERROR
        |Name4|Address4|ERROR
        |Name5/Address5/ERROR""".stripMargin
    StringParser.parse(s, defaultUserSettings) shouldBe Left(
      FileParsingError("Строка #1: Name1:Address1:ERROR"),
    )
  }

  it should "return notes list for purely structured data with left address" in {
    val singleNote1 = Note(
      1,
      "Address1:Info1 Name _  and more and , and more another information",
      "Spb Address1:Info1 Name _  and more and , and more ano",
    )
    val singleNote2 = Note(
      2,
      "Address2 inf NAme another letters, digits, words etc etc",
      "Spb Address2 inf NAme another letters, digits, words e",
    )
    val singleNote3 = Note(
      3,
      "Name3Address3,ERROR",
      "Spb Name3Address3,ERROR",
    )
    val s =
      """Address1:Info1 Name _  and more and , and more another information
        |Address2 inf NAme another letters, digits, words etc etc
        |Name3Address3,ERROR""".stripMargin
    StringParser.parse(
      s,
      defaultUserSettings.copy(nameCol = None, addrCol = Some(99), city = Some("Spb")),
    ) shouldBe Right(
      NotesWithInfo(List(singleNote1, singleNote2, singleNote3), StringParser.parseNoErrReport(3, 3)),
    )
  }

  //Right(NotesWithInfo(List(Note(1,Address1:Info1 Name _  and more and , and more another information,Address1:Info1 Name _  and more and , and more ano,None,None), Note(2,Address2 inf NAme another letters, digits, words etc etc,Address2 inf NAme another letters, digits, words e,None,None), Note(3,Name3Address3,ERROR,Name3Address3,ERROR,None,None)),Данные успешно разобраны (3 из 3)!
  //Right(NotesWithInfo(List(Note(1,Address1:Info1 Name _  and more and , and more another information, Address1:Info1 Name _  and more and , and more ano,None,None), Note(2,Address2 inf NAme another letters, digits, words etc etc, Address2 inf NAme another letters, digits, words e,None,None), Note(3,Name3Address3,ERROR, Name3Address3,ERROR,None,None)),Данные успешно разобраны (3 из 3)!
}
