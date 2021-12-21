import sbt._

object Dependencies {

    // Versions
    lazy val catsVersion = "2.6.1"
    lazy val catsEffectVersion = "3.1.1"
    lazy val sttpClientVersion = "3.3.11"
    lazy val circeVersion = "0.14.1"
    lazy val scalaLoggingVersion = "3.9.4"
    lazy val logBackVersion = "1.2.5"
    lazy val munitVersion = "0.7.27"
    lazy val scoptVersion = "4.0.1"
    lazy val specs2Version = "5.0.0-RC-03"

    // Libraries
    val catsCore = "org.typelevel" %% "cats-core" % catsVersion
    val catsEffect = "org.typelevel" %% "cats-effect" % catsEffectVersion
    val sttpCore = "com.softwaremill.sttp.client3" %% "core" % sttpClientVersion
    val sttpFs2 = "com.softwaremill.sttp.client3" %% "async-http-client-backend-fs2" % sttpClientVersion
    val circeCore = "io.circe" %% "circe-core" % circeVersion
    val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
    val circeParser = "io.circe" %% "circe-parser" % circeVersion
    val circeYaml = "io.circe" %% "circe-yaml" % circeVersion
    val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion
    val logBack = "ch.qos.logback" % "logback-classic" % logBackVersion
    val specs2 = "org.specs2" %% "specs2-core" % specs2Version
    val specs2extra = "org.specs2" %% "specs2-matcher-extra" % specs2Version
    val scopt = "com.github.scopt" %% "scopt" % scoptVersion

    // Dep list
    val discordDeps = Seq(sttpCore, sttpFs2, circeCore, circeGeneric, circeParser)

    val cfgManager = Seq(scopt, circeYaml)

    val botDeps = Seq(sttpCore, sttpFs2)

    val mainDeps = Seq()

    val commonDeps = Seq(catsCore, catsEffect, logBack, scalaLogging, specs2 % Test, specs2extra % Test)

}
