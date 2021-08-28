package discord.model.api

case class TeamObject(
  icon: Option[String],
  id: String,
  name: String,
  ownerUserId: String,
  members: List[MemberObject]
)
