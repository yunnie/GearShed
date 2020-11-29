package shed.config

import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import cats._
import cats.data._
import cats.effect._
import cats.implicits._

import io.circe._
import io.circe.config.parser
import io.circe.generic.auto._
import io.circe.syntax._

case class DbConfig(
  driver: String, url: String, user: String, pass: String
)

case class ServerConfig(
  host: String, port: Int
)

object Config {

  implicit val decodeDbConfig: Decoder[DbConfig] = 
    new Decoder[DbConfig] {
      final def apply(c: HCursor): Decoder.Result[DbConfig] =
        for {
          driver <- c.downField("driver").as[String]
          url <- c.downField("url").as[String]
          user <- c.downField("user").as[String]
          pass <- c.downField("pass").as[String]
        } yield DbConfig(driver, url, user, pass)
    }

  implicit val decodeServerConfig: Decoder[ServerConfig] = 
    new Decoder[ServerConfig] {
      final def apply(c: HCursor): Decoder.Result[ServerConfig] = 
        for {
          host <- c.downField("host").as[String]
          port <- c.downField("port").as[Int]
        } yield ServerConfig(host, port)
    }
  
  val myDbConfig: IO[DbConfig] = 
    parser.decodePathF[IO, DbConfig]("db")

  val myServerConfig: IO[ServerConfig]  = 
    parser.decodePathF[IO, ServerConfig]("server")

  def createTransactor(dbConf: DbConfig): Transactor.Aux[IO,Unit] = {
    implicit val cs = IO.contextShift(ExecutionContexts.synchronous)
    val xa = Transactor.fromDriverManager[IO](
      dbConf.driver, 
      dbConf.url,
      dbConf.user,
      dbConf.pass,
      Blocker.liftExecutionContext(ExecutionContexts.synchronous)
    )
    xa
  }

  def createIoTransactor(dbConf: IO[DbConfig]): 
    IO[Transactor.Aux[IO,Unit]] = 
      dbConf.map(conf => createTransactor(conf))
}
