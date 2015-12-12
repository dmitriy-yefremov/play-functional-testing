import sbt._

name := "play-functional-testing"

version := "1.0-SNAPSHOT"

play.Project.playScalaSettings

IntegrationTestSettings.settings

lazy val root = project.in(file(".")).configs(IntegrationTest)
