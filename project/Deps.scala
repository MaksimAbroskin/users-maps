import sbt._

object Deps {
  object Versions {
    val cats = "2.2.0"
  }

  val cats = "org.typelevel" %% "cats-effect" % Versions.cats withSources() withJavadoc()

}
