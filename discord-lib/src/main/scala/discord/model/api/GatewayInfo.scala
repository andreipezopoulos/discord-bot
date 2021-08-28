package discord.model.api

case class GatewaySessionStartLimit(
    total: Int,
    remaining: Int,
    resetAfter: Long,
    maxConcurrency: Int
)

case class Gateway(
    url: String,
    shards: Int,
    limits: GatewaySessionStartLimit
)
