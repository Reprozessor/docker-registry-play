import com.typesafe.sbt.SbtNativePackager.NativePackagerKeys._

name := "docker-registry-play"

scalaVersion := "2.11.4"
scalacOptions ++= Seq("-feature", "-deprecation")

parallelExecution in Test := true

libraryDependencies ++= Seq(
  jdbc,
  "com.jsuereth" % "scala-arm_2.11" % "1.4",
  "com.gilt" %% "play-json-service-lib-2-3" % "1.0.0",
  "org.specs2" %% "specs2" % "2.3.11" % "test",
  "com.wordnik" %% "swagger-play2" % "1.3.12"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)

TwirlKeys.templateFormats += ("json" -> "com.gilt.play.json.templates.JsonFormat")

// scoverage

import scoverage.ScoverageSbtPlugin.ScoverageKeys._

coverageExcludedPackages := "<empty>;Reverse.*;views.json..*"
coverageMinimum := 30
coverageFailOnMinimum := true

// Docker
maintainer in Docker := "Henning Jacobs <henning.jacobs@zalando.de>"

dockerBaseImage in Docker := "zalando/openjdk:8u40-b09-2"
dockerExposedPorts in Docker := Seq(9000)
