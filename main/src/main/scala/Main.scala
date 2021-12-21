package main

import cats.effect.{IO, IOApp, ExitCode}
import bot.runBot
import main.model.RawConfig
import main.config.readConfig

object Main extends IOApp {
    override def run(args: List[String]): IO[ExitCode] =
        for
            cfg <- readConfig(args)
            _ <- runBot(cfg)
        yield
            ExitCode.Success
}
