import Deps._

name := "FinalProject"

version := "0.1"

scalaVersion := "2.13.5"

libraryDependencies ++= (cats ++ http4s ++ circe ++ fs2) :+ logback
