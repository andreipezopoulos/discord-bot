package cfg

import cats.data.NonEmptyList

case class CfgPath(
    tokens: NonEmptyList[String]
)

extension (pieceOfPath: String)
    def /(otherPieceOfPath: String) =
        CfgPath(NonEmptyList.of(pieceOfPath, otherPieceOfPath))

extension (cfgPath: CfgPath)
    def /(pieceOfPath: String) =
        cfgPath.copy(tokens = cfgPath.tokens ++ List(pieceOfPath))
    
    def buildForCmdLine =
        cfgPath.build(".")

    def buildForEnv =
        cfgPath.build("_").toUpperCase()
    
    private def build(separator: String) =
        cfgPath.tokens.toList.mkString(separator)
