package discord.helper

import discord.model.{DiscordHostInfo, DiscordBot}
import discord.model.api.{Intent, <|>, GatewayMessage, Gateway, GatewaySessionStartLimit}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import sttp.ws.WebSocketFrame
import fs2.Stream
import fs2.concurrent.Topic
import io.circe.Json
import io.circe.parser.parse as circeParse
import scala.concurrent.duration.*

def buildPipe(
    input: List[Stream[IO, WebSocketFrame.Data[_]]],
    sessionCtrl: Option[SessionCtrl] = None,
    topic: Option[Topic[IO, GatewayMessage[Json]]] = None
) =
    buildPipeV2(input.reduce(_ ++ _), sessionCtrl, topic)

private def buildPipeV2(
    input: Stream[IO, WebSocketFrame.Data[_]],
    sessionCtrl: Option[SessionCtrl] = None,
    topic: Option[Topic[IO, GatewayMessage[Json]]] = None
) =
    val bot = DiscordBot(
        token = "token",
        intents = Intent.GUILD_MESSAGES <|> Intent.GUILD_MESSAGE_REACTIONS,
        hostInfo = DiscordHostInfo("", "", None)
    )

    for
        elseTopic <- Stream.eval(Topic[IO, GatewayMessage[Json]])
        data = WebSocketPipeData(
            bot = bot,
            sessionCtrl = sessionCtrl.getOrElse(SessionCtrl(None)),
            topic = topic.getOrElse(elseTopic)
        )
        rsp <- discordWebSocketPipe(data)(input)
    yield
        rsp

extension (sessionCtrl: SessionCtrl)
    def newSession =
        val gateway = Gateway("", 0, GatewaySessionStartLimit(0, 0, 0, 0))
        val newSession = sessionCtrl.asDiscordSession(gateway).get
        Some(SessionCtrl(Some(newSession)))

extension (s: String)
    def emit =
        Stream.emit(WebSocketFrame.text(s))

    def seqNum(n: Int) =
        circeParse(s)
            .getOrElse(Json.Null)
            .hcursor
            .downField("s")
            .withFocus(_ => Json.fromInt(n))
            .top
            .map(_.noSpaces)
            .getOrElse("{}")

extension [T] (s: Stream[IO, T])
    def run = s.compile.toVector.unsafeRunSync()

extension (s: Stream[IO, WebSocketFrame.Data[_]])
    def setSeq(seq: Int) = s.run.last.text.seqNum(seq).emit

extension (ws: WebSocketFrame)
    def text =
        ws match
            case WebSocketFrame.Text(payload, _, _) => payload
            case _ => "WTF"
