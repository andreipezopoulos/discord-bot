package cfg

import io.circe.{Decoder, Encoder, Json, HCursor, ACursor, ParsingFailure, DecodingFailure}
import io.circe.yaml.parser
import scala.io.Source
import cats.effect.IO
import cats.data.NonEmptyList

extension [T] (struct: CfgStructure[T])
    def readConfigFromFile(obj: T, filePath: String): IO[Either[Throwable, T]] = IO.defer {
        for
            parsedData <- parseFile(filePath)
            cursorAtRootElem = parsedData.hcursor
        yield
            struct
                .opts
                .filter(_.readFrom.file)
                .foldLeft[Either[Throwable, T]](Right(obj)) { (acc, o) =>
                    acc.flatMap(parseAndAlter(_, findField(o.ext, cursorAtRootElem), o))
                }
                .changeExceptionAndConcatOnErrorMessage(s"While reading file [$filePath]: ")
    }

private def parseFile(fileName: String) = IO.defer {
    for
        content <- readFile(fileName).handleErrorWith { _ => IO.pure("") }
        r <- IO.fromEither {
            parser.parse(content)
                .concatOnErrorMessage(s"While reading file [$fileName]: ")
        }
    yield
        r
}

extension (result: Either[ParsingFailure, Json])
    private def concatOnErrorMessage(prefix: String) =
        result
            .swap
            .map(x => x.copy(message = s"$prefix${x.message}"))
            .swap

extension [T] (result: Either[Throwable, T])
    private def changeExceptionAndConcatOnErrorMessage(prefix: String) =
        result
            .swap
            .map(x => Exception(s"$prefix${x.getMessage}"))
            .swap

private def readFile(fileName: String) = IO {
    Source.fromFile(fileName).getLines.mkString("\n")
}

private def parseAndAlter[T](obj: T, cursor: ACursor, opt: Opt[T]) =
    if (cursor.failed)
        Right(obj)
    else
        opt._parser.map(_.parseAndAlter(obj, cursor)).getOrElse(Right(obj))

private def findField(cfg: CfgPath, cursor: ACursor): ACursor =
    val tokens = cfg.tokens
    val current = cursor.downField(tokens.head)
    if (tokens.tail.isEmpty)
        current
    else
        val n = CfgPath(NonEmptyList.ofInitLast(tokens.tail.drop(1), tokens.tail.head))
        findField(n, current)