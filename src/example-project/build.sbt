val scala3Version = "3.8.4"
lazy val root = project
  .in(file("."))
  .settings(
    name := "prdt-example-project",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    resolvers += "jitpack" at "https://jitpack.io",
    libraryDependencies += "org.scalameta" %% "munit" % "1.3.4" % Test,
    libraryDependencies += "org.scalameta" %% "munit-scalacheck" % "1.2.0" % Test,
    libraryDependencies += "de.tu-darmstadt.stg" %% "rdts" % "oopsla26-artifact"
  )
