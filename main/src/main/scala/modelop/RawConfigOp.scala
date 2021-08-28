package main.model

extension (self: RawConfig)
    def merge(other: RawConfig) =
        RawConfig(
            botToken = self.botToken.orElse(other.botToken),
            proxyHost = self.proxyHost.orElse(other.proxyHost),
            proxySchema = self.proxySchema.orElse(other.proxySchema),
            proxyPort = self.proxyPort.orElse(other.proxyPort)
        )
