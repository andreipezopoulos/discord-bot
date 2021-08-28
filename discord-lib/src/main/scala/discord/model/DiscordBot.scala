package discord.model

import discord.model.api.CombinedIntent

case class DiscordHostInfo(schema: String, hostname: String, port: Option[Int])

case class DiscordBot(token: String, hostInfo: DiscordHostInfo, intents: CombinedIntent)
