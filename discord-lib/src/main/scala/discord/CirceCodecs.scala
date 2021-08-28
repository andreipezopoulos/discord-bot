package discord

import io.circe.{Decoder, Encoder, Json, HCursor}
import discord.model.api.*
import io.circe.syntax.*
import io.circe.ACursor

extension (a: ACursor)
    def asOption[T](using Decoder[T]) = a.as[Option[T]]
    def asList[T](using Decoder[T]) = a.asOption[List[T]].map(_.getOrElse(List()))

object CirceCodecs:
    given Decoder[GatewaySessionStartLimit] with
        def apply(c: HCursor): Decoder.Result[GatewaySessionStartLimit] =
            for
                total           <- c.downField("total").as[Int]
                remaining       <- c.downField("remaining").as[Int]
                resetAfter      <- c.downField("reset_after").as[Long]
                maxConcurrency  <- c.downField("max_concurrency").as[Int]
            yield
                GatewaySessionStartLimit(
                    total = total,
                    remaining = remaining,
                    resetAfter = resetAfter,
                    maxConcurrency = maxConcurrency
                )

    given Decoder[Gateway] with
        def apply(c: HCursor): Decoder.Result[Gateway] =
            for
                url     <- c.downField("url").as[String]
                shards  <- c.downField("shards").as[Int]
                limits  <- c.downField("session_start_limit").as[GatewaySessionStartLimit]
            yield
                Gateway(
                    url = url,
                    shards = shards,
                    limits = limits
                )

    given Decoder[MemberObject] with
        def apply(c: HCursor): Decoder.Result[MemberObject] =
            for
                membershipState <- c.downField("membership_state").as[Int]
                permissions     <- c.downField("permissions").asList[String]
                teamId          <- c.downField("teamId").as[String]
                user            <- c.downField("user").as[UserObject]
            yield
                MemberObject(
                    membershipState = membershipState,
                    permissions = permissions,
                    teamId = teamId,
                    user = user
                )

    given Decoder[ApplicationObject] with
        def apply(c: HCursor): Decoder.Result[ApplicationObject] =
            for
                id                  <- c.downField("id").as[String]
                name                <- c.downField("name").asOption[String]
                icon                <- c.downField("icon").asOption[String]
                description         <- c.downField("description").asOption[String]
                rpcOrigins          <- c.downField("rpc_origins").asList[String]
                botPublic           <- c.downField("bot_public").asOption[Boolean]
                botRequireCodeGrant <- c.downField("bot_require_code_grant").asOption[Boolean]
                termsOfServiceUrl   <- c.downField("terms_of_service_url").asOption[String]
                privacyPolicyUrl    <- c.downField("privacy_policy_url").asOption[String]
                owner               <- c.downField("owner").asOption[UserObject]
                summary             <- c.downField("summary").asOption[String]
                verifyKey           <- c.downField("verify_key").asOption[String]
                team                <- c.downField("team").asOption[TeamObject]
                guildId             <- c.downField("guild_id").asOption[String]
                primarySkuId        <- c.downField("primary_skuid").asOption[String]
                slug                <- c.downField("slug").asOption[String]
                coverImage          <- c.downField("cover_image").asOption[String]
                flags               <- c.downField("flags").asOption[Int]
            yield
                ApplicationObject(
                    id = id,
                    name = name.getOrElse(""),
                    icon = icon,
                    description = description.getOrElse(""),
                    rpcOrigins = rpcOrigins,
                    botPublic = botPublic,
                    botRequireCodeGrant = botRequireCodeGrant,
                    termsOfServiceUrl = termsOfServiceUrl,
                    privacyPolicyUrl = privacyPolicyUrl,
                    owner = owner,
                    summary = summary.getOrElse(""),
                    verifyKey = verifyKey.getOrElse(""),
                    team = team,
                    guildId = guildId,
                    primarySkuId = primarySkuId,
                    slug = slug.getOrElse(""),
                    coverImage = coverImage,
                    flags = flags
                )

    given Decoder[UnavailableGuildObject] with
        def apply(c: HCursor): Decoder.Result[UnavailableGuildObject] =
            for
                id          <- c.downField("id").as[String]
                unavailable <- c.downField("unavailable").as[Boolean]
            yield
                UnavailableGuildObject(
                    id = id,
                    unavailable = unavailable
                )

    given Decoder[ReadyResponse] with
        def apply(c: HCursor): Decoder.Result[ReadyResponse] =
            for
                v           <- c.downField("v").as[Int]
                sessionId   <- c.downField("session_id").as[String]
                shard       <- c.downField("shard").asList[Int]
                //user        <- c.downField("user").as[UserObject]
                //application <- c.downField("application").as[ApplicationObject]
                guilds      <- c.downField("guilds").asList[UnavailableGuildObject]
            yield
                ReadyResponse(
                    v = v,
                    sessionId = sessionId,
                    shard = shard,
                    //user = user,
                    //application = application,
                    guilds = guilds
                )

    given Decoder[TeamObject] with
        def apply(c: HCursor): Decoder.Result[TeamObject] =
            for
                icon        <- c.downField("icon").asOption[String]
                id          <- c.downField("id").as[String]
                name        <- c.downField("name").as[String]
                ownerUserId <- c.downField("owner_user_id").as[String]
                members     <- c.downField("members").asList[MemberObject]
            yield
                TeamObject(
                    id = id,
                    icon = icon,
                    name = name,
                    ownerUserId = ownerUserId,
                    members = members
                )

    given Decoder[UserObject] with
        def apply(c: HCursor): Decoder.Result[UserObject] =
            for
                id              <- c.downField("id").as[String]
                username        <- c.downField("username").as[String]
                discriminator   <- c.downField("discriminator").as[String]
                avatar          <- c.downField("avatar").asOption[String]
                bot             <- c.downField("bot").asOption[Boolean]
                system          <- c.downField("system").asOption[Boolean]
                mfaEnabled      <- c.downField("mfa_enabled").asOption[Boolean]
                locale          <- c.downField("locale").asOption[String]
                verified        <- c.downField("verified").asOption[Boolean]
                email           <- c.downField("email").asOption[String]
                flags           <- c.downField("flags").asOption[Int]
                premiumType     <- c.downField("premium_type").asOption[Int]
                publicFlags     <- c.downField("public_flags").asOption[Int]
            yield
                UserObject(
                    id = id,
                    username = username,
                    discriminator = discriminator,
                    avatar = avatar,
                    bot = bot,
                    system = system,
                    mfaEnabled = mfaEnabled,
                    locale = locale,
                    verified = verified,
                    email = email,
                    flags = flags,
                    premiumType = premiumType,
                    publicFlags = publicFlags
                )

    given Decoder[HeartBeatPayload] with
        def apply(c: HCursor): Decoder.Result[HeartBeatPayload] =
            for
                interval <- c.downField("heartbeat_interval").as[Int]
            yield
                HeartBeatPayload(interval)

    given [R](using Decoder[R]): Decoder[GatewayMessage[R]] with
        def apply(c: HCursor): Decoder.Result[GatewayMessage[R]] =
            for
                t  <- c.downField("t").asOption[String]
                s  <- c.downField("s").asOption[Int]
                op <- c.downField("op").as[Int]
                d  <- c.downField("d").asOption[R]
            yield
                GatewayMessage(
                    eventName = t,
                    sequence = s,
                    opcode = op,
                    data = d
                )

    given [R](using Encoder[R]): Encoder[ClientMessage[R]] = new Encoder[ClientMessage[R]] {
        def apply(c: ClientMessage[R]): Json = Json.obj(
            ("op", Json.fromInt(c.opcode)),
            ("d", c.data.asJson)
        )
    }

    given Encoder[IdentifyRequest] with
        def apply(c: IdentifyRequest): Json = Json.obj(
            ("token", Json.fromString(c.token)),
            ("intents", Json.fromLong(c.intents.value)),
            ("properties", Json.obj(
                 ("$browser", Json.fromString(c.properties.browser)),
                 ("$device", Json.fromString(c.properties.device)),
                 ("$os", Json.fromString(c.properties.os))
            ))
        )

    given Encoder[ResumeRequest] with
        def apply(c: ResumeRequest): Json = Json.obj(
            ("token", Json.fromString(c.token)),
            ("session_id", Json.fromString(c.sessionId)),
            ("seq", Json.fromInt(c.sequence))
        )

end CirceCodecs
