package discord.model.api

case class MemberObject(
  membershipState: Int,
  permissions: List[String],
  teamId: String,
  user: UserObject
)
