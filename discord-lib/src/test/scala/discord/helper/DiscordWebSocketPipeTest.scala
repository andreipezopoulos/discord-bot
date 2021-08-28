package discord.helper

import discord.model.api.GatewayMessage
import cats.effect.IO
import sttp.ws.WebSocketFrame
import cats.effect.IO.asyncForIO
import fs2.Stream
import fs2.concurrent.Topic
import io.circe.Json
import org.specs2.*
import org.specs2.matcher.*
import org.specs2.matcher.JsonMatchers.*
import scala.language.implicitConversions
import scala.concurrent.duration.*

class DiscordWebSocketPipeTest extends Specification:

    def is = s2"""
    This is a specification from DiscordWebSocketPipe
        Pipe should post all received messages on topic $topicValidation
        Pipe should respond the Gateway Hello request $gtHello
        Pipe should respond the Heart Beat message $heartBeat
        Pipe should disconnect if gateway doesn't beat back $gtNoHeartBeat
        Pipe should be able to resume connection $resumeConn
        Pipe should be able to reset connection when is not possible to resume $resetConn
        Pipe should terminate if gateway request reconnection $reconnByGt
     """

    def gtHello =
        val msgs = List(emit(Payload.gatewayHello))
        val pipe = buildPipe(msgs)
        val responses = pipe.take(2).run

        responses must haveSize(2)

        val identify = responses(0).text
        identify must / ("op" -> 2)
        identify must / ("d") / ("token" -> "Bot token")
        identify must / ("d") / ("intents" -> 1536)
        identify must / ("d") / ("properties") / ("$browser" -> not(beEmpty[String]))
        identify must / ("d") / ("properties") / ("$device" -> not(beEmpty[String]))
        identify must / ("d") / ("properties") / ("$os" -> not(beEmpty[String]))

        val heartBeat = responses(1).text
        heartBeat must / ("op" -> 1)
        heartBeat must / ("d" -> null.asInstanceOf[String])

    def heartBeat =
        val msgs = List(
            emit(Payload.gatewayHello),

            emit(Payload.anyRequest)
                setSeq 34,

            emit(Payload.heartBeat)
                delayBy 3.seconds,

            emit(Payload.anyRequest)
                setSeq 82 delayBy 3.seconds
        )

        val pipe = buildPipe(msgs)
        val responses = pipe.take(3).run

        responses must haveSize(3)

        val fstHeartBeat = responses(1).text
        fstHeartBeat must / ("op" -> 1)
        fstHeartBeat must / ("d" -> 34)

        val sndHeartBeat = responses(2).text
        sndHeartBeat must / ("op" -> 1)
        sndHeartBeat must / ("d" -> 82)

    def gtNoHeartBeat =
        val msgs = List(
            emit(Payload.gatewayHello),
            emit(Payload.anyRequest)
        )
        val pipe = buildPipe(msgs)
        pipe.take(3).run must throwAn[Exception](message = "Heart beat response wasn't received")

    def resumeConn =
        val msgs = List(
            emit(Payload.gatewayHello),
            emit(Payload.ready),
            emit(Payload.anyRequest)
                setSeq 8,
        )
        val session = SessionCtrl(None)
        val pipe = buildPipe(msgs, Some(session))

        pipe.take(2).run

        val newMsgs = List(
            emit(Payload.gatewayHello),

            emit(Payload.resume)
                setSeq 2 delayBy 2.seconds,

            emit(Payload.heartBeat)
                delayBy 4.seconds
        )
        val newSession = session.newSession
        val newPipe = buildPipe(newMsgs, newSession)
        val responses = newPipe.take(3).run

        responses must haveSize(3)

        val resume = responses(0).text
        resume must / ("op" -> 6)
        resume must / ("d") / ("seq" -> 8)
        resume must / ("d") / ("session_id" -> "abdce")
        resume must / ("d") / ("token" -> "Bot token")

        newSession.get.maybeSessionId.get must beEqualTo("abdce")
        newSession.get.maybeSequence.get must beEqualTo(2)

    def resetConn =
        val msgs = List(
            emit(Payload.gatewayHello),
            emit(Payload.ready),
            emit(Payload.anyRequest)
                setSeq 8,
        )
        val session = SessionCtrl(None)
        val pipe = buildPipe(msgs, Some(session))

        pipe.take(2).run

        val newMsgs = List(
            emit(Payload.gatewayHello),
            emit(Payload.reset)
        )
        val newSession = session.newSession
        val newPipe = buildPipe(newMsgs, newSession)
        val responses = newPipe.take(2).run

        responses must haveSize(2)

        val resume = responses(0).text
        resume must / ("op" -> 6)
        resume must / ("d") / ("seq" -> 8)
        resume must / ("d") / ("session_id" -> "abdce")
        resume must / ("d") / ("token" -> "Bot token")

        val identify = responses(1).text
        identify must / ("op" -> 2)
        identify must / ("d") / ("token" -> "Bot token")
        identify must / ("d") / ("intents" -> 1536)
        identify must / ("d") / ("properties") / ("$browser" -> not(beEmpty[String]))
        identify must / ("d") / ("properties") / ("$device" -> not(beEmpty[String]))
        identify must / ("d") / ("properties") / ("$os" -> not(beEmpty[String]))

    def reconnByGt =
        val msgs = List(emit(Payload.reconnect))
        val pipe = buildPipe(msgs)
        pipe.take(1).run must throwAn[Exception](message = "Gateway requested reconnection")

    def topicValidation =
        val msgs = List(
            emit(Payload.gatewayHello),
            emit(Payload.ready),
            emit(Payload.anyRequest)
        )

        val pipe =
            for
                topic <- Stream.eval(Topic[IO, GatewayMessage[Json]])
                sub = topic.subscribe(20)
                rsp <- buildPipe(msgs, topic = Some(topic)).either(sub)
            yield
                rsp

        val fromTopic = pipe.take(5).run.filter(_.isRight).map(_.toOption.get)

        fromTopic must haveSize(3)

        val hello = fromTopic(0)
        hello.opcode must beEqualTo(10)

        val ready = fromTopic(1)
        ready.opcode must beEqualTo(0)
        ready.eventName.get must beEqualTo("READY")

        val other = fromTopic(2)
        other.opcode must beEqualTo(999)
        other.eventName.get must beEqualTo("ANYTHING")

    def emit(s: String) = Stream.emit(WebSocketFrame.text(s))

end DiscordWebSocketPipeTest
