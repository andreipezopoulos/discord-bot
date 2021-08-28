package main.config

import cats.effect.IO
import scopt.OParser
import main.model.CmdLineConfig

def readConfigurationFromCmdLine(args: List[String]): IO[CmdLineConfig] =
    IO.defer {
        OParser.parse(argParser, args, CmdLineConfig()) match {
            case Some(config) =>
                IO.pure(config)
            case _ =>
                IO.raiseError(Exception("What?"))
        }
    }

private val builder = OParser.builder[CmdLineConfig]

private val argParser = {
    import builder.*
    OParser.sequence(
        programName("discord-bot"),
        head("discord-bot", "0.x"),

        opt[String]('c', "cfgpath")
            .action((path, c) => c.copy(configPath = Some(path)))
            .text("Config path"),

        opt[String]('t', "token")
            .action((token, c) => c.copy(config = c.config.copy(botToken = Some(token))))
            .text("bot token"),

        opt[String]('u', "proxy.host")
            .action((host, c) => c.copy(config = c.config.copy(proxyHost = Some(host))))
            .text("Proxy host"),

        opt[String]('s', "proxy.schema")
            .action((schema, c) => c.copy(config = c.config.copy(proxySchema = Some(schema))))
            .text("Proxy schema (ws or wss)"),

        opt[Int]('p', "proxy.port")
            .action((port, c) => c.copy(config = c.config.copy(proxyPort = Some(port))))
            .text("Proxy port")
    )
}
