package main

import cats.effect.{IO, IOApp, ExitCode}
import main.config.readConfig
import bot.runBot

object Main extends IOApp {
    override def run(args: List[String]): IO[ExitCode] =
        for
            cfg <- readConfig(args)
            _ <- runBot(cfg)
        yield
            ExitCode.Success
}
