package discord.model.api

case class GatewayMessage[T](
    eventName: Option[String],
    sequence: Option[Int],
    opcode: Int,
    data: Option[T]
)
