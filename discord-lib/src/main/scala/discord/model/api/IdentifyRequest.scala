package discord.model.api

case class ClientProperties(os: String, browser: String, device: String)

case class IdentifyRequest(token: String, intents: CombinedIntent, properties: ClientProperties)
