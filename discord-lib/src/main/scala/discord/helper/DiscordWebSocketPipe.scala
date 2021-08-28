package discord.helper

import cats.effect.IO
import discord.model.api.{ResumeRequest, GatewayMessage, ClientMessage, HeartBeatPayload, IdentifyRequest, ClientProperties, ReadyResponse, asClientMessage}
import discord.model.{DiscordBot, DiscordSession, genTokenString}
import io.circe.{Decoder, Json}
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import discord.CirceCodecs.given
import fs2.*
import fs2.concurrent.Topic
import sttp.ws.WebSocketFrame
import java.time.LocalDateTime
import com.typesafe.scalalogging.Logger

def discordWebSocketPipe(data: WebSocketPipeData): Pipe[IO, WebSocketFrame.Data[_], WebSocketFrame] =
    (input: Stream[IO, WebSocketFrame.Data[_]]) =>

        val heartBeat = HeartBeatStream()

        val pipe =
            for
                raw    <- input
                _      <- logInput(raw)
                parsed <- parse(raw)
                seq    <- data.sessionCtrl.setSequence(parsed)
                _      <- heartBeat.setSequence(seq)
                _      <- heartBeat.handleHeartBeatRequest(parsed)
                _      <- Stream.eval(data.topic.publish1(parsed))
                rsp    <- handleRequest(data, parsed)
            yield
                rsp

        pipe
            .merge(heartBeat.stream)
            .map { rsp =>
                WebSocketFrame.text(rsp.asJson.noSpaces)
            }
            .logOutput()

def handleRequest(data: WebSocketPipeData, payload: GatewayMessage[Json]): Stream[IO, ClientMessage[Json]] =
    payload.opcode match
        case 7 =>
            Stream.raiseError[IO](new Exception("Gateway requested reconnection"))

        case 9 =>
            val rsp = loginPayload(data.bot, None)
            Stream.emit(rsp)

        case 10 =>
            val rsp = loginPayload(data.bot, data.sessionCtrl.lastSession)
            Stream.emit(rsp)

        case 0 =>
            payload.eventName.getOrElse("") match
                case "READY" =>
                    payload.data.get.as[ReadyResponse]
                        .fold(
                            fb = { parsed => Stream.eval(data.sessionCtrl.setSessionId(parsed.sessionId)).drain },
                            fa = { err => Stream.raiseError(err) }
                        )

                case "RESUMED" =>
                    Stream
                        .eval(data.sessionCtrl.setCurrentFromLastSession)
                        .drain

                case _ =>
                    Stream.empty

        case _ =>
            Stream.empty

end handleRequest

def loginPayload(bot: DiscordBot, lastSession: Option[DiscordSession]): ClientMessage[Json] =
    lastSession
        .map(d =>
            val data =
                ResumeRequest(
                    token = bot.genTokenString,
                    sessionId = d.sessionId,
                    sequence = d.sequence
                )

            data.asClientMessage(6)
        )
        .getOrElse {
            val data =
                IdentifyRequest(
                    token = bot.genTokenString,
                    intents = bot.intents,
                    properties = ClientProperties(
                        os = "Linux", // FIXME
                        device = "test_bot",
                        browser = "test_bot"
                    )
                )

            data.asClientMessage(2)
        }
end loginPayload

extension (sessionCtrl: SessionCtrl)

    def setSequence(payload: GatewayMessage[Json]): Stream[IO, Option[Int]] =
        payload
            .sequence
            .map { seq =>
                Stream
                    .eval(sessionCtrl.setSequence(seq))
                    .map(Some(_))
            }
            .getOrElse(Stream.emit(None))

extension (heartBeat: HeartBeatStream)

    def setSequence(seq: Option[Int]): Stream[IO, Unit] =
        def stream(sequence: Int) =
            val signal = SequenceReceivedSignal(sequence)
            Stream
                .eval(heartBeat.sendSignal(signal))

        seq
            .map(stream(_))
            .getOrElse(Stream.emit { val x = 1 })

    def handleHeartBeatRequest(payload: GatewayMessage[Json]): Stream[IO, GatewayMessage[Json]] =
        val stream = payload.opcode match
            case 1 =>
                Stream
                    .eval(heartBeat.sendSignal(ResetBeatingSignal))

            case 10 =>
                def createStream(interval: Int) =
                    val signal = BeginBeatingSignal(interval / 1000)
                    Stream
                        .eval(heartBeat.sendSignal(signal))

                payload.data.get.as[HeartBeatPayload]
                    .map(hd => createStream(hd.interval))
                    .getOrElse(Stream.empty)

            case 11 =>
                Stream
                    .eval(heartBeat.sendSignal(BeatResponseReceivedSignal))

            case _ =>
                Stream
                    .emit(payload)

        stream.map(_ => payload)

end extension

def parse(input: WebSocketFrame.Data[_]): Stream[IO, GatewayMessage[Json]] =
    input match
        case WebSocketFrame.Text(payload, _, _) =>
            decode[GatewayMessage[Json]](payload)
                .fold(
                    fb = { data => Stream.emit(data) },
                    fa = { err => Stream.raiseError(err) }
                )

        case _ => Stream.empty
