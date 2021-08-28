package main.config

import io.circe.{Decoder, Encoder, Json, HCursor}
import io.circe.yaml.parser
import scala.io.Source
import main.model.RawConfig
import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeError

def readConfigFromFile(path: String): IO[RawConfig] =
    read(path)
        .handleError(_ => RawConfig())

private def read(path: String) =
    for
        content <- IO { Source.fromFile(path).getLines.mkString }
        raw = parser.parse(content).toOption.get
        parsed <- raw.as[RawConfig] match {
            case Left(msg) => IO.raiseError(Exception(msg))
            case Right(obj) => IO.pure(obj)
        }
    yield
        parsed

private given Decoder[RawConfig] with
    def apply(c: HCursor): Decoder.Result[RawConfig] =
        for
            token   <- c.downField("token").as[Option[String]]
            proxy   = c.downField("proxy")
            host    <- proxy.downField("host").as[Option[String]]
            schema  <- proxy.downField("schema").as[Option[String]]
            port    <- proxy.downField("port").as[Option[Int]]
        yield
            RawConfig(
                botToken = token,
                proxyHost = host,
                proxySchema = schema,
                proxyPort = port
            )
