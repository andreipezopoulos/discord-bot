package cfg.internal

import io.circe.ACursor
import io.circe.Decoder.Result

trait OptParser[T]:
    def parseAndAlter(obj: T, cursor: ACursor): Either[Throwable, T]
    def parseAndAlter(obj: T, value: String): Either[Throwable, T]

private inline def parseAndAlterCursor[T, R](obj: T, result: Result[R], alter: (T, R) => T) = 
    result.map { alter(obj, _) }

private inline def parseAndAlterTry[T, R](obj: T, v: String, alter: (T, R) => T, parseFunc: (String) => R) =
    val x = try
        Right(parseFunc(v))
    catch
        case e: Exception => Left(e)
    x.map(alter(obj, _))

class IntOptParser[T](val alter: (T, Int) => T) extends OptParser[T]:
    override def parseAndAlter(obj: T, cursor: ACursor) = parseAndAlterCursor(obj, cursor.as[Int], alter)
    override def parseAndAlter(obj: T, value: String)  = parseAndAlterTry(obj, value, alter, { (_:String).toInt })

class StringOptParser[T](val alter: (T, String) => T) extends OptParser[T]:
    override def parseAndAlter(obj: T, cursor: ACursor) = parseAndAlterCursor(obj, cursor.as[String], alter)
    override def parseAndAlter(obj: T, value: String)  = parseAndAlterTry(obj, value, alter, { (x: String) => x })

class LongOptParser[T](val alter: (T, Long) => T) extends OptParser[T]:
    override def parseAndAlter(obj: T, cursor: ACursor) = parseAndAlterCursor(obj, cursor.as[Long], alter)
    override def parseAndAlter(obj: T, value: String)  = parseAndAlterTry(obj, value, alter, { (_:String).toLong })

class DoubleOptParser[T](val alter: (T, Double) => T) extends OptParser[T]:
    override def parseAndAlter(obj: T, cursor: ACursor) = parseAndAlterCursor(obj, cursor.as[Double], alter)
    override def parseAndAlter(obj: T, value: String)  = parseAndAlterTry(obj, value, alter, { (_:String).toDouble })
