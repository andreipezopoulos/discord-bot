package discord

import cats.effect.IO
import sttp.client3.*
import sttp.client3.asynchttpclient.fs2.AsyncHttpClientFs2Backend
import discord.model.{DiscordBot, DiscordHostInfo, DiscordSession, DiscordRecvTopic}
import discord.helper.{SessionCtrl, WebSocketPipeData}
import discord.model.api.{Gateway, GatewayMessage}
import io.circe.{Decoder, Json}
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import discord.CirceCodecs.given
import sttp.capabilities.fs2.Fs2Streams
import discord.helper.discordWebSocketPipe
import discord.DiscordGatewayBotExtensions
import fs2.Stream
import fs2.concurrent.Topic
import scala.concurrent.duration.*
import com.typesafe.scalalogging.Logger
import cats.implicits.catsSyntaxApplicativeError

object DiscordGatewayBotExtensions:

    extension (db: DiscordBot)
        def getGatewayInfo: IO[Either[String, Gateway]] =
            val uri = uri"${db.hostInfo.prefix}/gateway/bot"

            val request = basicRequest
                .header("Authorization", s"Bot ${db.token}")
                .get(uri)

            httpClient.use { backend =>
                for {
                    response <- backend.send(request)
                } yield (response.body.discordRsp[Gateway])
            }

        def loopConnect(topic: DiscordRecvTopic, wsProxy: Option[DiscordHostInfo] = None): IO[Nothing] =
            val extractGateway = db.getGatewayInfo
                .flatMap { i => IO.fromEither(i.swap.map(Exception(_)).swap) }
                .map(GatewayInfo(_, wsProxy))

            loopConnect(
                getGatewayInfo = extractGateway,
                topic = topic,
                backoff = minBackoffConnectMs,
                lastSession = None
            )

        private def loopConnect(getGatewayInfo: IO[GatewayInfo], lastSession: Option[DiscordSession], topic: DiscordRecvTopic, backoff: Int): IO[Nothing] =
            val apiInteraction =
                for
                    gateway <- getGatewayInfo
                    errorAndSession <- connect(gateway, lastSession, topic)
                yield
                    errorAndSession

            for
                errorAndSession <- apiInteraction.handleError((_, None))
                (error, maybeSession) = errorAndSession
                _ <- IO(logger.warn(s"Backing off for ${backoff}ms, error [${error.getMessage}]", error))
                _ <- IO.blocking(Thread.sleep(backoff))
                r <- loopConnect(
                    getGatewayInfo = getGatewayInfo,
                    lastSession = maybeSession,
                    topic = topic,
                    backoff =
                        maybeSession
                            .map(_ => minBackoffConnectMs)
                            .getOrElse(backoff * 2)
                            .min(maxBackoffConnectMs)
                )
            yield
                r

        private def connect(
            gatewayInfo: GatewayInfo,
            lastSession: Option[DiscordSession],
            topic: DiscordRecvTopic
        ): IO[Tuple2[Throwable, Option[DiscordSession]]] =
            val uri = uri"${gatewayInfo.uri}?v=9&encoding=json"

            val socketData = WebSocketPipeData(
                sessionCtrl = SessionCtrl(lastSession),
                bot = db,
                topic = topic
            )

            val request = emptyRequest
                .response(asWebSocketStream(Fs2Streams[IO])(discordWebSocketPipe(socketData)))
                .header("Host", gatewayInfo.host)
                .get(uri)

            wsClient.use { backend =>
                request
                    .send(backend)
                    .attempt
                    .map { (whatHappened: Either[Throwable, Response[Either[String, Unit]]]) =>
                        whatHappened
                            .fold(
                                fa = { e => e },
                                fb = { rsp =>
                                    val httpErrorBody = rsp.body.swap.getOrElse("<NO BODY>")
                                    Exception(s"Http Error Body [$httpErrorBody]")
                                }
                            )
                    }
                    .map { (error: Throwable) =>
                        (error, socketData.sessionCtrl.asDiscordSession(gatewayInfo.discordGateway))
                    }
            }

    end extension

    private val httpClient = AsyncHttpClientFs2Backend.resource[IO]()
    private val wsClient = AsyncHttpClientFs2Backend.resource[IO]()
    private val maxBackoffConnectMs = 60_000
    private val minBackoffConnectMs = 100
    private class GatewayBot
    private val logger = Logger[GatewayBot]

end DiscordGatewayBotExtensions

private case class GatewayInfo(discordGateway: Gateway, proxy: Option[DiscordHostInfo])

extension (gt: GatewayInfo)
    def uri =
        val originalUri = uri"${gt.discordGateway.url}"
        gt.proxy match
            case Some(p) =>
                val urlWithoutPort = originalUri.host(p.hostname).scheme(p.schema)
                p.port match
                    case Some(port) => urlWithoutPort.port(port)
                    case None => urlWithoutPort

            case None => originalUri

    def host = uri"${gt.discordGateway.url}".host

extension (e: Either[String, String])
    def discordRsp[T](using Decoder[T]) = e.flatMap(decode[T](_)).swap.map(_.toString).swap

extension (d: DiscordHostInfo)
    def prefix = s"${d.schema}://${d.hostname}/api/v9"
