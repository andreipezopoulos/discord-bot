package bot.model

import discord.model.DiscordHostInfo

case class BotConfig(
    botToken: String,
    proxy: DiscordHostInfo
)
