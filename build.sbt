name := "PowerUtil"
version := "0.0.1"

scalaVersion := "2.13.3"

scalacOptions ++= Seq("-deprecation")

crossPaths := false

scalacOptions ++= Seq("-encoding", "UTF-8")

autoScalaLibrary := false

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-library" % scalaVersion.value,
  "net.java.dev.jna" % "jna" % "5.5.0",
  "net.java.dev.jna" % "jna-platform" % "5.5.0",
)
