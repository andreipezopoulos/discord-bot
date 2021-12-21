package cfg

import cfg.internal.{BuildOptParser, OptParser}

case class Opt[T](
    ext: CfgPath,
    readFrom: ReadOpt,
    _parser: Option[OptParser[T]] = None,
    _help: Option[String] = None
):  
    def action[R](using parser: BuildOptParser[T, R])(f: (T, R) => T) =
        copy(_parser = Some(parser.build(f)))
    
    def help(text: String) =
        copy(_help = Some(text))
