import Deps._

lazy val `users-maps` = (project in file("."))
  .settings(
    version := "0.1",
    scalaVersion := "2.13.5",
    libraryDependencies ++= dependencies,
    dockerExposedPorts += 8080,
    dockerBaseImage := "adoptopenjdk/openjdk11",
    dockerRepository := Some("eu.gcr.io/dins-scala-school"),
    Docker / version := s"${git.gitHeadCommit.value.map(_.take(7)).getOrElse("UNKNOWN")}",
    dockerUpdateLatest := true,
    Universal / mappings += (sourceDirectory.value / "main" / "resources" / "map.html", "resources/map.html")
  )
  .enablePlugins(DockerPlugin, JavaAppPackaging)

lazy val dependencies =
  (cats ++ http4s ++ circe ++ fs2 ++ doobie ++ tests ++ testContainers ++ mock) :+ logback :+ flyway

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

//enablePlugins(FlywayPlugin)
//lazy val postgres = "postgres"
//
//lazy val postgresHost: String = sys.env.getOrElse("DB_HOST", postgres)
//lazy val postgresPort: String = sys.env.getOrElse("DB_PORT", "5432")
//lazy val postgresName: String = sys.env.getOrElse("DB_NAME", postgres)
//lazy val postgresUser: String = sys.env.getOrElse("DB_USER", postgres)
//lazy val postgresPass: String = sys.env.getOrElse("DB_PASS", postgres)
//
////libraryDependencies += "mysql" % "mysql-connector-java" % "6.0.6"
////flywayUrl := "jdbc:mysql://localhost:3306/test_flyway"
//flywayUrl := s"jdbc:postgresql://localhost:$postgresPort/$postgresName"
//flywayUser := postgresUser
//flywayPassword := postgresPass
//flywayLocations += "db/migration"
