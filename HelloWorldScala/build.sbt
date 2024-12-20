ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.4"

lazy val root = (project in file("."))
  .settings(
    name := "HelloWorldScala"
  )

libraryDependencies += "io.github.jwharm.javagi" % "gtk" % "0.11.0"