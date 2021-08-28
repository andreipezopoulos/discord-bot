package discord.model.api

case class ReadyResponse(
    v: Int,
    sessionId: String,
    shard: List[Int],
    //user: UserObject,
    //application: ApplicationObject,
    guilds: List[UnavailableGuildObject]
)
