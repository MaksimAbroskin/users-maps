import Deps._
import sbt.Keys.scalaVersion

lazy val `users-maps` = (project in file("."))
  .settings(
    version := "0.1",
    scalaVersion := "2.13.5",
    libraryDependencies ++= dependencies,
    dockerExposedPorts += 8080,
    dockerBaseImage := "adoptopenjdk/openjdk11",
    dockerRepository := Some("eu.gcr.io/dins-scala-school"),
    version in Docker := s"${git.gitHeadCommit.value.map(_.take(7)).getOrElse("UNKNOWN")}",
    dockerUpdateLatest := true
  )
  .enablePlugins(DockerPlugin, JavaAppPackaging)

lazy val dependencies =
  (cats ++ http4s ++ circe ++ fs2 ++ doobie ++ apache ++ tests ++ testContainers ++ mock) :+ logback

ThisBuild / scalacOptions ++= Seq(
  "-Xlint:unused",
  "-Xfatal-warnings",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-deprecation",
  "-language:higherKinds",
  "-Ypatmat-exhaust-depth",
  "40",
)
