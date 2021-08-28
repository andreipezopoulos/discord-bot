package discord.model.api

extension (d: CombinedIntent)
    def value: Long =
        val leftValue =
            d.left match
                case x: Intent => (1L << x.i)
                case x: CombinedIntent => x.value

        val rightValue =
            d.right.map(_.value).getOrElse(0L)

        leftValue | rightValue

    def combine(other: CombinedIntent | Intent): CombinedIntent =
        CombinedIntent(other, Some(d))

    def <|>(other: Intent): CombinedIntent =
        combine(other)

    def <|>(other: CombinedIntent): CombinedIntent =
        combine(other)

extension (intent: Intent)
    def combine(other: Intent): CombinedIntent =
        CombinedIntent(intent) <|> other

    def <|>(other: Intent): CombinedIntent =
        combine(other)
