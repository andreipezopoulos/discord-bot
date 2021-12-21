package main.config

import main.model.RawConfig
import cfg.*

private val builder = cfg.Builder[RawConfig]
import builder.{*, given}

val cfgStructure = CfgStructure[RawConfig](
    programName = "discord-bot",
    version = "0.0.1",
    cfgFilePath = Some(CfgFilePathCmdLine("cfgFile", 'f', "path of configuration file")),
    opts = List(
        opt("token", FromEnv() | FromCmdLine('t') | FromFile())
            .action[String]((c, token) => c.copy(botToken = Some(token)))
            .help("Bot's token"),
            
        opt("proxy" / "host", FromEnv() | FromCmdLine('u') | FromFile())
            .action[String]((c, host) => c.copy(proxyHost = Some(host)))
            .help("Proxy's host"),

        opt("proxy" / "schema", FromEnv() | FromCmdLine('s') | FromFile())
            .action[String]((c, schema) => c.copy(proxySchema = Some(schema)))
            .help("Proxy's schema"),
        
        opt("proxy" / "port", FromEnv() | FromCmdLine('p') | FromFile())
            .action[Int]((c, port) => c.copy(proxyPort = Some(port)))
            .help("Proxy's port")
    )
)
