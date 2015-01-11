name := "sprocrest"

scalaVersion := "2.11.4"
scalacOptions ++= Seq("-feature", "-deprecation")

parallelExecution in Test := true

libraryDependencies ++= Seq(
  jdbc,
  "com.jsuereth" % "scala-arm_2.11" % "1.4",
  "com.gilt" %% "play-json-service-lib-2-3" % "1.0.0",
  "org.specs2" %% "specs2" % "2.3.11" % "test"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)

TwirlKeys.templateFormats += ("json" -> "com.gilt.play.json.templates.JsonFormat")

// scoverage

import scoverage.ScoverageSbtPlugin.ScoverageKeys._

coverageExcludedPackages := "<empty>;Reverse.*;views.json..*"
coverageMinimum := 30
coverageFailOnMinimum := true


