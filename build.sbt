val scala3Version = "3.1.0"

import scala.sys.process._

ThisBuild / scalaVersion := scala3Version

lazy val commonSettings = Seq(
  resolvers += Resolver.mavenLocal,
  libraryDependencies ++= Dependencies.commonDeps,
)

lazy val cfgManager = (project in file("cfg-manager"))
  .settings(
    name := "cfg-manager",
    version := "0.0.1",
    commonSettings,
    libraryDependencies ++= Dependencies.cfgManager
)

lazy val discordLib = (project in file("discord-lib"))
  .settings(
    name := "discord-lib",
    version := "0.0.1",
    commonSettings,
    libraryDependencies ++= Dependencies.discordDeps
)

lazy val bot = (project in file("bot"))
  .dependsOn(discordLib)
  .settings(
    name := "bot",
    version := "0.0.1",
    commonSettings,
    libraryDependencies ++= Dependencies.botDeps
  )

lazy val proxyArgs = " -u localhost -p 8020"

lazy val main = (project in file("main"))
  .dependsOn(discordLib, bot, cfgManager)
  .settings(
    name := "discord-bot",
    version := "0.0.1",
    commonSettings,
    libraryDependencies ++= Dependencies.mainDeps,

    startProxy := {
        val log = streams.value.log
        val output = new java.io.ByteArrayOutputStream

        (Commands.dockerPs #> output) ! log

        val mustRunProxy = output.toString().isEmpty()
        if (mustRunProxy) {
            val abspath = new java.io.File("nginx.conf").getAbsolutePath()
            val cmd = Commands.dockerRun
            log.debug(cmd)
            cmd ! log
        }
    },

    stopProxy := {
        val log = streams.value.log
        Commands.dockerRm ! log
    },

    (Compile / run) := {
        startProxy.value
        (Compile / run).partialInput(proxyArgs).evaluated
    },

    // Those lines are not working =/
    (Compile / run / mainClass) := Some("main.Main"),
    (Compile / packageBin / mainClass) := Some("main.Main"),

    runWithProxy := {
        (Compile / run).evaluated
        val log = streams.value.log
        Commands.dockerRm ! log
    }
  )

lazy val root = (project in file("."))
  .aggregate(main, discordLib)
  .settings(
    genDepGraph := {
        (Compile / dependencyDot).value
        "dot target/dependencies-compile.dot -o target/out.svg -Tsvg" !
    }
  )

lazy val genDepGraph = inputKey[Unit]("Generate lib deps graph")
lazy val startProxy = taskKey[Unit]("Start discord proxy")
lazy val stopProxy = taskKey[Unit]("Stop discord proxy")
lazy val runWithProxy = inputKey[Unit]("Run wrapped by a discord proxy")
