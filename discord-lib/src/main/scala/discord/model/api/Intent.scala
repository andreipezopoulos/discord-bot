package discord.model.api

enum Intent(val i: Int):
    case GUILDS                     extends Intent(0)
    case GUILD_MEMBERS              extends Intent(1)
    case GUILD_BANS                 extends Intent(2)
    case GUILD_EMOJIS               extends Intent(3)
    case GUILD_INTEGRATIONS         extends Intent(4)
    case GUILD_WEBHOOKS             extends Intent(5)
    case GUILD_INVITES              extends Intent(6)
    case GUILD_VOICE_STATUS         extends Intent(7)
    case GUILD_PRESENCES            extends Intent(8)
    case GUILD_MESSAGES             extends Intent(9)
    case GUILD_MESSAGE_REACTIONS    extends Intent(10)
    case GUILD_MESSAGE_TYPING       extends Intent(11)
    case DIRECT_MESSAGES            extends Intent(12)
    case DIRECT_MESSAGE_REACTIONS   extends Intent(13)
    case DIRECT_MESSAGE_TYPING      extends Intent(14)

case class CombinedIntent(
    left: Intent | CombinedIntent,
    right: Option[CombinedIntent] = None
)
