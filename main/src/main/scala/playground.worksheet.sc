
import cats.effect.unsafe.implicits.*
import cats.effect.IO
import fs2.{Stream, Pipe, Chunk, Pull}
import fs2.concurrent.{Topic, SignallingRef}
import scala.concurrent.duration.*

extension [T] (s: Stream[IO, T])
    def run() = s.compile.toVector.unsafeRunSync()

def s1(s: Stream[IO, Int]) =
  s.map(_ + 1)

def s2(s: Stream[IO, Int]) =
  s.map(_ + 2)

val x = Stream.awakeEvery[IO](1.second).map { _ => 1 }.take(3)

s1(x).merge(s2(x)).run()
