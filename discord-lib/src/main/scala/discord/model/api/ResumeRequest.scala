package discord.model.api

case class ResumeRequest(token: String, sessionId: String, sequence: Int)
