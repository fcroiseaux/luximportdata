name := "LuxDataImport"

organization := "com.intech"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"
)

initialCommands := "import com.intech.luxdataimport._"

