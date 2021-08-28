package discord.model.api

case class ClientMessage[T](
    opcode: Int,
    data: Option[T]
)
