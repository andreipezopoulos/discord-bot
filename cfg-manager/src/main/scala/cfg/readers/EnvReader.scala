package cfg

import cats.effect.IO
import cats.implicits.*
import cfg.internal.OptParser

extension [T] (struct: CfgStructure[T])

    def readFromEnv(obj: T): IO[Either[Throwable, T]] = IO.defer {
        for
            listOfEnvValueAndOpt <- struct
                .opts
                .filter(_.readFrom.env)
                .map { opt =>
                    getEnv(opt.ext.buildForEnv).map((_, opt))
                }
                .sequence
        yield
            listOfEnvValueAndOpt
                .foldLeft[Either[Throwable, T]](Right(obj)) { (acc, valueAndOpt) =>
                    val (maybeValue: Option[String], opt: Opt[T]) = valueAndOpt
                    opt._parser.map(parseAndAlterFromEnv(maybeValue, _, obj)).getOrElse(acc)
                }
    }

private def parseAndAlterFromEnv[T](maybeValue: Option[String], parser: OptParser[T], obj: T) =
    maybeValue.map(parser.parseAndAlter(obj, _)).getOrElse(Right(obj))

private def getEnv(name: String) = IO { Option(System.getenv(name)) }
