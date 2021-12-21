package cfg.internal

trait BuildOptParser[T, R]:
    def build(action: (T, R) => T): OptParser[T]
