package discord.model.api

case class ApplicationObject(
  id: String,
  name: String,
  icon: Option[String],
  description: String,
  rpcOrigins: List[String],
  botPublic: Option[Boolean],
  botRequireCodeGrant: Option[Boolean],
  termsOfServiceUrl: Option[String],
  privacyPolicyUrl: Option[String],
  owner: Option[UserObject],
  summary: String,
  verifyKey: String,
  team: Option[TeamObject],
  guildId: Option[String],
  primarySkuId: Option[String],
  slug: String,
  coverImage: Option[String],
  flags: Option[Int]
)
