package discord.model.api

import io.circe.{Encoder, Json}
import io.circe.syntax.*

extension [T] (data: T)

    def asClientMessage(op: Int)(using Encoder[T]) =
        ClientMessage[Json](
            opcode = op,
            data = Some(data.asJson)
        )
