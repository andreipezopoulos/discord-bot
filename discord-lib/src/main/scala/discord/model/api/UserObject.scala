package discord.model.api

case class UserObject(
    id: String,
    username: String,
    discriminator: String,
    avatar: Option[String],
    bot: Option[Boolean],
    system: Option[Boolean],
    mfaEnabled: Option[Boolean],
    locale: Option[String],
    verified: Option[Boolean],
    email: Option[String],
    flags: Option[Int],
    premiumType: Option[Int],
    publicFlags: Option[Int]
)
