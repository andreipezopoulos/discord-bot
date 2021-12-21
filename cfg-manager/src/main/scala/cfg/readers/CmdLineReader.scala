package cfg

import cats.effect.IO
import scopt.OParser

extension [T] (struct: CfgStructure[T])

    def readConfigFromArgs(args: List[String], obj: T): IO[T] = IO.defer {
        OParser.parse(struct.generateScoptStructure(obj), args, obj) match {
            case Some(config) =>
                IO.pure(config)
            case _ =>
                IO.raiseError(Exception("Some command line parameter was wrong"))
        }
    }

    def extractFilePath(args: List[String]): Option[String] =
        buildToReadFilePath()
            .flatMap { parser =>
                import scopt.OParser
                OParser.runParser(parser, args, "") match {
                    case (result, effects) =>
                        OParser.runEffects(effects, new scopt.DefaultOEffectSetup {
                            override def displayToOut(msg: String): Unit = ()
                            override def displayToErr(msg: String): Unit = ()
                            override def reportError(msg: String): Unit = ()
                            override def reportWarning(msg: String): Unit = ()
                            override def terminate(exitState: Either[String, Unit]): Unit = ()
                        })

                    result
                }
            }
            .filter { filePath => !(filePath.isEmpty) }

    private def generateScoptStructure(exampleObj: T) =
        val builder = OParser.builder[T]
        import builder.*

        val header: scopt.OParser[Unit, T] =
            OParser.sequence(
                programName(struct.programName),
                head(struct.programName, struct.version)
            )

        val initial =
            struct.cfgFilePath
                .map { cfgFilePathCfg =>
                    header ++ OParser.sequence(cfgFilePathCfg.buildOpt)
                }
                .getOrElse(header)
        
        struct
            .opts
            .filter(_.readFrom.cmdLine)
            .foldLeft(initial) { (acc, opt) =>
                val char = extractCmdLetter(opt.readFrom)

                val scoptOpt =
                    builder.opt[String](char, opt.ext.buildForCmdLine)
                        .text(opt._help.getOrElse(""))
                        .validate(validateFuncToScopt(opt, _, exampleObj, builder))
                        .action((v, obj) => parseAndAlter(opt, v, obj))

                acc ++ OParser.sequence(scoptOpt)
            }

    private def buildToReadFilePath() =
        val builder = OParser.builder[String]
        import builder.*
        
        struct.cfgFilePath
            .map { cfgFilePathCfg =>

                val initial: scopt.OParser[Unit, String] =
                    OParser.sequence(
                        programName(""),
                        head("", ""),
                        cfgFilePathCfg.buildOpt.action((v, _) => v)
                    )
                
                struct
                    .opts
                    .filter(_.readFrom.cmdLine)
                    .foldLeft(initial) { (acc, opt) =>
                        val char = extractCmdLetter(opt.readFrom)

                        val scoptOpt =
                            builder.opt[String](char, opt.ext.buildForCmdLine)

                        acc ++ OParser.sequence(scoptOpt)
                    }
            }

extension (self: CfgFilePathCmdLine)
    private def buildOpt[T] =
        val builder = OParser.builder[T]
        import builder.*
        builder.opt[String](self.cmdLineLetter, self.fullName)
            .text(self.helpText)

private def extractCmdLetter(readOpt: ReadOpt) =
    readOpt match {
        case FromCmdLine(c) => c
        case FromEnvAndCmdLine(c) => c.c
        case FromFileAndCmdLine(c) => c.c
        case FromAll(c) => c.c
        case FromFile() | FromEnv() | FromFileAndEnv() => throw Exception("what???")
    }

private def validateFuncToScopt[T](opt: Opt[T], value: String, obj: T, builder: scopt.OParserBuilder[T]) =
    opt._parser
        .map(_.parseAndAlter(obj, value)
            .fold({ x => builder.failure(x.getMessage) }, { _ => builder.success })
        )
        .getOrElse(builder.success)

private def parseAndAlter[T](opt: Opt[T], value: String, obj: T) =
    opt._parser
        .map(_.parseAndAlter(obj, value).fold({ ex => throw ex}, { x => x}))
        .getOrElse(obj)
