package cfg

import cats.data.NonEmptyList
import cfg.internal.{BuildOptParser, IntOptParser, StringOptParser, LongOptParser, DoubleOptParser}

class Builder[T]:

    given BuildOptParser[T, Int] with
        override def build(action: (T, Int) => T) = IntOptParser(action)

    given BuildOptParser[T, String] with
        override def build(action: (T, String) => T) = StringOptParser(action)

    given BuildOptParser[T, Double] with
        override def build(action: (T, Double) => T) = DoubleOptParser(action)

    given BuildOptParser[T, Long] with
        override def build(action: (T, Long) => T) = LongOptParser(action)

    def opt(ext: CfgPath, readFrom: ReadOpt): Opt[T] =
        Opt[T](ext = ext, readFrom)
    
    def opt(ext: String, readFrom: ReadOpt): Opt[T] =
        Opt[T](ext = CfgPath(NonEmptyList.one(ext)), readFrom)
    