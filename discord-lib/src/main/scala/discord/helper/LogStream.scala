package discord.helper

import sttp.ws.WebSocketFrame
import com.typesafe.scalalogging.Logger
import io.circe.{Decoder, Json}
import io.circe.parser.decode
import cats.effect.IO
import fs2.Stream

private val logger = Logger("discord.helper.WebSocketStream")

def logInput(data: WebSocketFrame.Data[_]) =
    val io =
        data match
            case WebSocketFrame.Text(payload, _, _) =>
                for
                    //_ <- IO(logger.debug(s"Raw received [$payload] message"))
                    _ <- payload.debugPrettyJson("Received ")
                yield
                    data

            case _ =>
                IO(logger.debug(s"Received some unknown message"))

    Stream.eval(io)

extension (s: Stream[IO, WebSocketFrame])
    def logOutput() = s.flatMap { data =>
        val io =
            data match
                case WebSocketFrame.Text(payload, _, _) =>
                    for
                        _ <- payload.debugPrettyJson("Sent ")
                    yield
                        data

                case _ =>
                    IO(logger.debug(s"Sent some unknown message"))

        Stream.eval(io).map(_ => data)
    }

extension (s: String)
    def debugPrettyJson(prefix: String) =
        IO {
            logger.debug(prefix + decode[Json](s).map(_.toString).toString)
        }
