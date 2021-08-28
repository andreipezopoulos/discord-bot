package discord.model

extension (bot: DiscordBot)

    def genTokenString =
        s"Bot ${bot.token}"
