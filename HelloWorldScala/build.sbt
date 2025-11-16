ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.4"

lazy val root = (project in file("."))
  .settings(
    name := "HelloWorldScala",
    fork := true,
    javaOptions += "--enable-native-access=ALL-UNNAMED"
  )

libraryDependencies += "org.java-gi" % "gtk" % "0.13.0"
