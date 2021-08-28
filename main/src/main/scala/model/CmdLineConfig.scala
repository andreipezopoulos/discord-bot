package main.model

case class CmdLineConfig(
    config: RawConfig = RawConfig(),
    configPath: Option[String] = None
)
