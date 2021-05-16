package ru.dins.scalaschool.file_to_map

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import ru.dins.scalaschool.file_to_map.Models.{FileParsingError, InfoMessage, Note}

class FileParserTest extends AnyFlatSpec with Matchers {
  private val note1 = Note(1, "Name1", "Address1")
  private val note2 = Note(2, "Name2", "Address2")
  private val note3 = Note(3, "Name3", "Address3")

  private val lineDelimiter = "'"
  private val inRowDelimiter = ";"

  private val parseNoErrReport = s"File parsed successful!\n\nFetching coordinates in process. Please, wait..."
  private def parseWithErrReport(success: Int, total: Int) =
    s"File parsed.\nSuccessful: $success out of $total\nFor more details call /deepParse\n\nFetching coordinates in process. Please, wait..."

//  "parse" should "return notes list if string is correct" in {
//    val s =
//      """Name1;Address1'
//        |Name2;Address2'
//        |Name3;Address3'""".stripMargin
//    FileParser.parse(s, lineDelimiter, inRowDelimiter) shouldBe Right((List(note1, note2, note3), InfoMessage(parseNoErrReport)))
//  }
//
//  it should "return notes list if string have some formatting troubles" in {
//    val s =
//      """ Name1;Address1 'Name2 ;Address2'Name3; Address3 '"""
//    FileParser.parse(s, lineDelimiter, inRowDelimiter) shouldBe Right((List(note1, note2, note3), InfoMessage(parseNoErrReport)))
//  }
//
//  it should "return parsing error if line delimiter incorrect" in {
//    val s =
//      """Name1;Address1\
//        |Name2;Address2\
//        |Name3;Address3\""".stripMargin
//    FileParser.parse(s, lineDelimiter, inRowDelimiter) shouldBe Left(FileParsingError("""Line #1: Name1;Address1\Name2;Address2\Name3;Address3\"""))
//  }
//
//  it should "return parsing error if in row delimiter incorrect" in {
//    val s =
//      """Name1,Address1'
//        |Name2,Address2'
//        |Name3,Address3'""".stripMargin
//    FileParser.parse(s, lineDelimiter, inRowDelimiter) shouldBe Left(FileParsingError("""Line #1: Name1,Address1"""))
//  }
//
//  it should "return notes list if at least one row parsed successful" in {
//    val s =
//      """Name1;Address1;ERROR'
//        |Name2;Address2'
//        |Name3;Address3;ERROR'
//        |Name4;Address4;ERROR'
//        |Name5;Address5;ERROR'""".stripMargin
//    FileParser.parse(s, lineDelimiter, inRowDelimiter) shouldBe Right(List(note2), InfoMessage(parseWithErrReport(1, 5)))
//  }
//
//  it should "return error if no successful parsed rows" in {
//    val s =
//      """Name1;Address1;ERROR'
//        |Name2;Address2;ERROR'
//        |Name3;Address3;ERROR'
//        |Name4;Address4;ERROR'
//        |Name5;Address5;ERROR'""".stripMargin
//    FileParser.parse(s, lineDelimiter, inRowDelimiter) shouldBe Left(FileParsingError("""Line #1: Name1;Address1;ERROR"""))
//  }


}
