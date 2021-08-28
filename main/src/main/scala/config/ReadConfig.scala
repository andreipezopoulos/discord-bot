package main.config

import cats.effect.IO
import discord.model.DiscordHostInfo
import main.model.{RawConfig, merge}
import bot.model.BotConfig

def readConfig(cmdLineArgs: List[String]): IO[BotConfig] =
    for
        cmdLineCfg <- readConfigurationFromCmdLine(cmdLineArgs)

        cfgFilePath = cmdLineCfg.configPath.getOrElse(Defaults.configFilePath)
        fileCfg <- readConfigFromFile(cfgFilePath)

        rawConfig = cmdLineCfg.config.merge(fileCfg)
        finalConfig <- map(rawConfig).toIO()
    yield
        finalConfig

extension [A] (x: Either[String, A])

    def toIO() = x match {
        case Left(msg) => IO.raiseError(Exception(msg))
        case Right(obj) => IO.pure(obj)
    }

private def map(c: RawConfig): Either[String, BotConfig] =
    for
        botToken <- c.botToken.toRight("You should configure the Bot Token. See help for more details.")
        proxyHost = c.proxyHost.getOrElse(Defaults.proxyHost)
        proxySchema = c.proxySchema.getOrElse(Defaults.proxySchema)
    yield
        BotConfig(
            botToken = botToken,
            proxy = DiscordHostInfo(
                schema = proxySchema,
                hostname = proxyHost,
                port = c.proxyPort
            )
        )
