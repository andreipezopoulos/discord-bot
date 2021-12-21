package cfg

sealed trait ReadOpt {
    val file: Boolean
    val env: Boolean
    val cmdLine: Boolean
}

case class FromFile() extends ReadOpt {
    override val file = true
    override val env = false
    override val cmdLine = false
}

case class FromEnv() extends ReadOpt {
    override val file = false
    override val env = true
    override val cmdLine = false
}

case class FromCmdLine(val c: Char) extends ReadOpt {
    override val file = false
    override val env = false
    override val cmdLine = true
}

case class FromFileAndEnv() extends ReadOpt {
    override val file = true
    override val env = true
    override val cmdLine = false
}

case class FromEnvAndCmdLine(val cmdLineOpts: FromCmdLine) extends ReadOpt {
    override val file = false
    override val env = true
    override val cmdLine = true
}

case class FromFileAndCmdLine(val cmdLineOpts: FromCmdLine) extends ReadOpt {
    override val file = true
    override val env = false
    override val cmdLine = true
}

case class FromAll(val cmdLineOpts: FromCmdLine) extends ReadOpt {
    override val file = true
    override val env = true
    override val cmdLine = true
}

extension (self: FromFile)
    def |(other: FromEnvAndCmdLine) =
        FromAll(other.cmdLineOpts)

    def |(other: FromEnv) =
        FromFileAndEnv()

    def |(other: FromCmdLine) =
        FromFileAndCmdLine(other)

extension (self: FromEnv)
    def |(other: FromFileAndCmdLine) =
        FromAll(other.cmdLineOpts)
    
    def |(other: FromFile) =
        FromFileAndEnv()

    def |(other: FromCmdLine) =
        FromEnvAndCmdLine(other)
    
extension (self: FromCmdLine)
    def |(other: FromFileAndEnv) =
        FromAll(self)
    
    def |(other: FromFile) =
        FromFileAndCmdLine(self)
    
    def |(other: FromEnv) =
        FromEnvAndCmdLine(self)
    
extension (self: FromFileAndEnv)
    def |(other: FromCmdLine) = FromAll(other)

extension (self: FromEnvAndCmdLine)
    def |(other: FromFile) = FromAll(self.cmdLineOpts)

extension (self: FromFileAndCmdLine)
    def |(other: FromEnv) = FromAll(self.cmdLineOpts)
