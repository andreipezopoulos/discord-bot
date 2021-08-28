package discord.model

import discord.model.api.Gateway

case class DiscordSession(sessionId: String, sequence: Int, gateway: Gateway)
