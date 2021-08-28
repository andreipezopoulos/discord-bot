package main.model

case class RawConfig(
    botToken: Option[String] = None,
    proxyHost: Option[String] = None,
    proxySchema: Option[String] = None,
    proxyPort: Option[Int] = None
)
