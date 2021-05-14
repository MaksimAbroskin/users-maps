package ru.dins.scalaschool.file_to_map

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import ru.dins.scalaschool.file_to_map.Models.{FileParsingError, Note}

class FileParserTest extends AnyFlatSpec with Matchers {
  val note1: Note = Note(1, "Name1", "Address1")
  val note2: Note = Note(2, "Name2", "Address2")
  val note3: Note = Note(3, "Name3", "Address3")
  val note4: Note = Note(4, "Name4", "Address4")
  val note5: Note = Note(5, "Name5", "Address5")
  val note6: Note = Note(6, "Name6", "Address6")
  val note7: Note = Note(7, "Name7", "Address7")
  val note8: Note = Note(8, "Name8", "Address8")
  val note9: Note = Note(9, "Name9", "Address9")
  val note10: Note = Note(10, "Name10", "Address10")
  val note11: Note = Note(11, "Name11", "Address11")

  var lineDelimiter: String = "'"
  var inRowDelimiter: String = ";"

  "parse" should "return notes list if string is correct" in {
    val s =
      """Name1;Address1'
        |Name2;Address2'
        |Name3;Address3'""".stripMargin
    FileParser.parse(s, lineDelimiter, inRowDelimiter) shouldBe Right(List(note1, note2, note3))
  }

  it should "return notes list if string have some formatting troubles" in {
    val s =
      """ Name1;Address1 'Name2 ;Address2'Name3; Address3 '"""
    FileParser.parse(s, lineDelimiter, inRowDelimiter) shouldBe Right(List(note1, note2, note3))
  }

  it should "return parsing error if line delimiter incorrect" in {
    val s =
      """Name1;Address1\
        |Name2;Address2\
        |Name3;Address3\""".stripMargin
    FileParser.parse(s, lineDelimiter, inRowDelimiter) shouldBe Left(FileParsingError("""Line #1: Name1;Address1\Name2;Address2\Name3;Address3\"""))
  }

  it should "return parsing error if in row delimiter incorrect" in {
    val s =
      """Name1,Address1'
        |Name2,Address2'
        |Name3,Address3'""".stripMargin
    FileParser.parse(s, lineDelimiter, inRowDelimiter) shouldBe Left(FileParsingError("""Line #3: Name3,Address3"""))
  }

  it should "return notes list if less then 10% rows are incorrect" in {
    val s =
      """Name1;Address1'
        |Name2;Address2'
        |Name3;Address3'
        |Name4;Address4'
        |Name5;Address5'
        |Name6;Address6'
        |Name7;Address7;ERROR'
        |Name8;Address8'
        |Name9;Address9'
        |Name10;Address10'
        |Name11;Address11'""".stripMargin
    FileParser.parse(s, lineDelimiter, inRowDelimiter) shouldBe Right(List(note1, note2, note3, note4, note5, note6, note8, note9, note10, note11))
  }

  it should "return error if more then 10% rows are incorrect" in {
    val s =
      """Name1;Address1'
        |Name2;Address2'
        |Name3;Address3'
        |Name4;Address4'
        |Name5;Address5'
        |Name6;Address6'
        |Name7;Address7;ERROR'
        |Name8;Address8'
        |Name9;Address9;ERROR'
        |Name10;Address10'
        |Name11;Address11'""".stripMargin
    FileParser.parse(s, lineDelimiter, inRowDelimiter) shouldBe Left(FileParsingError("""Line #9: Name9;Address9;ERROR"""))
  }

  "parseWithErrInfo" should "return numbers of failed rows" in {
    val s =
      """Name1;Address1;Error'
        |Name2;Address2;Error'
        |Name3;Address3'""".stripMargin
    FileParser.parseWithErrInfo(s, lineDelimiter, inRowDelimiter) shouldBe FileParsingError(s"Numbers of failed rows:\n1, 2")
  }

}
