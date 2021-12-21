package cfg

import cats.effect.IO
import cats.data.NonEmptyList

extension [T] (struct: CfgStructure[T])

    def read(args: List[String], obj: T) =
        IO.defer {
            for
                r1 <- struct.readFromEnv(obj)
                r2 <- IO.fromEither { r1 }
                cfgFilePath = struct.extractFilePath(args)
                r3 <- struct.readConfigFromFile(r2, cfgFilePath.getOrElse(defaultCfgFilePath))
                r4 <- IO.fromEither { r3 }
                r5 <- struct.readConfigFromArgs(args, r4)
            yield
                r5
        }

private val defaultCfgFilePath = "./config.yaml"
