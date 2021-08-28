package bot

import cats.effect.IO
import fs2.concurrent.Topic
import fs2.Stream
import io.circe.Json
import com.typesafe.scalalogging.Logger
import discord.model.api.GatewayMessage
import bot.model.BotConfig

def runBot(cfg: BotConfig) =
    import discord.model.*
    import discord.model.api.*
    import discord.DiscordGatewayBotExtensions.*

    val discordBot = DiscordBot(
        token = cfg.botToken,
        intents = Intent.GUILD_MESSAGES <|> Intent.GUILD_MESSAGE_REACTIONS,
        hostInfo = DiscordHostInfo(
            schema = "https",
            hostname = "discord.com",
            port = None
        )
    )

    val proxy = Some(cfg.proxy)

    for
        _ <- logConfiguration(cfg)
        topic <- Topic[IO, GatewayMessage[Json]]
        loopConnect = discordBot.loopConnect(topic, proxy)
        stream = topic.subscribe(10).foreach(recvMsg)
        discordLoop = Stream.eval(loopConnect)
        r <- discordLoop.concurrently(stream).compile.drain
    yield
       r

private def recvMsg(msg: GatewayMessage[Json]) = IO {
    println(s"RECV ${msg}")
}

private class Bot
private val logger = Logger[Bot]
private def logConfiguration(cfg: BotConfig) = IO {
    val msg = s"""
Starting bot with the following configuration:
  * Proxy: ${cfg.proxy.toString}
  * Token: ${cfg.botToken.mask(5, 5)}
"""
    logger.info(msg)
}

extension (str: String)

    def mask(s: Int, e: Int) =
        str.take(s) + "..." + str.takeRight(e)
