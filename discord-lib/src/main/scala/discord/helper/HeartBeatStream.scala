package discord.helper

import java.util.concurrent.atomic.{AtomicInteger, AtomicBoolean, AtomicReference}
import java.time.LocalDateTime
import fs2.Stream
import sttp.ws.WebSocketFrame
import cats.effect.IO
import discord.model.api.ClientMessage
import scala.concurrent.duration.*
import discord.CirceCodecs.given
import io.circe.syntax.EncoderOps
import io.circe.{Json}

private case class ExtractedBeatData(shouldBeat: Boolean, lastSequence: Int, brokenConnection: Boolean)

case class HeartBeatConfig(minBeatTimeout: Int, pollingEvery: FiniteDuration)

class HeartBeatStream(val config: HeartBeatConfig = HeartBeatConfig(4, 2.seconds)):

    // This definition must be here (before "stream" definition) or fiber will throw exception at some point lol
    private val extractBeatData =
        for
            now           <- IO(LocalDateTime.now)
            maybeLastBeat <- IO(lastBeat.get)
            lastSeq       <- IO(lastSequence.get)
            canBeat       <- IO(canBeat.get)

            whenShouldBeat = maybeLastBeat.getOrElse(now.minusSeconds(1))
            shouldBeat     = canBeat && whenShouldBeat.isBefore(now)
            rspRecv        = shouldBeat && balanceMsg.getAndIncrement == 0

            _ <- IO(if shouldBeat then lastBeat.set(Some(now.plusSeconds(healthInterval.get))))
        yield
            ExtractedBeatData(shouldBeat, lastSeq, !rspRecv)

    val stream: Stream[IO, ClientMessage[Json]] =
        for
            data <- Stream.repeatEval(extractBeatData).metered(config.pollingEvery)
            if data.shouldBeat
            _    <- brokeStreamIfBrokenHeartBeat(data.brokenConnection)
        yield
            ClientMessage[Json](
                opcode = 1,
                data = data.lastSequence.takeIf(_ >= 0).map(_.asJson)
            )

    def sendSignal(signal: HeartBeatSignal): IO[Unit] =
        IO {
            signal match
                case BeginBeatingSignal(interval) =>
                    val i = if interval < config.minBeatTimeout then config.minBeatTimeout else interval
                    if canBeat.get then
                        healthInterval.set(i)
                    else
                        healthInterval.set(i)
                        canBeat.set(true)

                case ResetBeatingSignal =>
                    lastBeat.set(null)

                case BeatResponseReceivedSignal =>
                    balanceMsg.getAndDecrement

                case SequenceReceivedSignal(seq) =>
                    lastSequence.set(seq)
        }

    private def brokeStreamIfBrokenHeartBeat(brokenConnection: Boolean): Stream[IO, Boolean] =
        if brokenConnection then
            Stream.raiseError[IO](new Exception("Heart beat response wasn't received"))
        else
            Stream.emit(true)

    private val lastSequence = new AtomicInteger(-1)
    private val canBeat = new AtomicBoolean(false)
    private val healthInterval = new AtomicInteger(600)
    private val lastBeat = new AtomicReference[Option[LocalDateTime]](None)
    private val balanceMsg = new AtomicInteger(0)

end HeartBeatStream
