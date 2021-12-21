package cfg

case class CfgFilePathCmdLine(
    fullName: String,
    cmdLineLetter: Char,
    helpText: String
)

case class CfgStructure[T](
    programName: String,
    version: String,
    opts: List[Opt[T]],
    cfgFilePath: Option[CfgFilePathCmdLine] = None
)
