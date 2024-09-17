import Dependencies.*

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.3"

lazy val root = (project in file("."))
  .settings(name := "site-name-parser")
  .aggregate(`site-name-parser-api`, `site-name-parser-impl`)

lazy val `site-name-parser-api` = (project in file("site-name-parser/api"))
  .settings(libraryDependencies ++= apiDependencies)

lazy val `site-name-parser-impl` = (project in file("site-name-parser/impl"))
  .settings(libraryDependencies ++= implDependencies)
  .dependsOn(`site-name-parser-api`)