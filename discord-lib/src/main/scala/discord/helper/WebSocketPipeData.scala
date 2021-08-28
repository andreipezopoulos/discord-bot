package discord.helper

import discord.model.DiscordBot
import discord.model.api.GatewayMessage
import io.circe.Json
import cats.effect.IO
import fs2.concurrent.Topic
import fs2.Stream

case class WebSocketPipeData(
    bot: DiscordBot,
    sessionCtrl: SessionCtrl,
    topic: Topic[IO, GatewayMessage[Json]]
)
