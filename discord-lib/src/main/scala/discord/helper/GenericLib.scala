package discord.helper

extension [T] (d: T)
    def takeIf(f: (T) => Boolean) =
        if (f(d)) then Some(d) else None
