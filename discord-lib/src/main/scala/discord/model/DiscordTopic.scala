package discord.model

import fs2.concurrent.Topic
import discord.model.api.GatewayMessage
import io.circe.Json
import cats.effect.IO

type DiscordRecvTopic = Topic[IO, GatewayMessage[Json]]
