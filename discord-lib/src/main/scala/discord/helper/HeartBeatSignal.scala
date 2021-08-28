package discord.helper

sealed trait HeartBeatSignal

case class BeginBeatingSignal(intervalSeconds: Int) extends HeartBeatSignal

case class SequenceReceivedSignal(sequence: Int) extends HeartBeatSignal

object ResetBeatingSignal extends HeartBeatSignal

object BeatResponseReceivedSignal extends HeartBeatSignal
