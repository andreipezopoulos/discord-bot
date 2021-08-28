package discord.helper

import java.util.concurrent.atomic.{AtomicReference, AtomicInteger}
import cats.effect.IO
import discord.model.api.Gateway
import discord.model.DiscordSession

class SessionCtrl(val lastSession: Option[DiscordSession]){

    // These two needs to be here or they will be null at some point
    private val sessionId =
        AtomicReference[Option[String]](None)

    private val sequence =
        AtomicInteger(-1)

    def asDiscordSession(g: Gateway): Option[DiscordSession] =
        maybeSequence
            .zip(maybeSessionId)
            .map((seq, id) => DiscordSession(sessionId = id, sequence = seq, gateway = g))

    def setSessionId(sessionId: String) =
        IO {
            this.sessionId.set(Some(sessionId))
            sessionId
        }

    def setSequence(sequence: Int) =
        IO {
            this.sequence.set(sequence)
            sequence
        }

    val setCurrentFromLastSession =
        lastSession
            .map { d =>
                if maybeSessionId.isEmpty then
                    setSessionId(d.sessionId)
                else
                    IO.pure("")
            }
            .getOrElse(IO.pure(""))

    def maybeSessionId =
        sessionId.get

    def maybeSequence =
        val seq = sequence.get
        if seq < 0 then None else Some(seq)

    override def toString() =
        s"SessionCtrl(sessionId = ${sessionId.get}, sequence = ${sequence.get}, lastSession = ${lastSession})"
}
