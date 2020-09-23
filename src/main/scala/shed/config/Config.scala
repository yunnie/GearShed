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

  /* 
   * Add implicit decoders to decode the configuration 
   * file into DbConfig and ServerConfig class objects 
   */
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
  
  /* 
   * Read in the configuration files and create
   * a DbConfig object and a ServerConfig object
   */

  val myDbConfig: IO[DbConfig] = 
    parser.decodePathF[IO, DbConfig]("db")

  val myServerConfig: IO[ServerConfig]  = 
    parser.decodePathF[IO, ServerConfig]("server")

  /* 
   * Create a database transactor to run the doobie queries
   */
  def createTransactor(dbConf: DbConfig): Transactor.Aux[IO,Unit] = {
    // Create a simple transactor for development purposes
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

  /* Wrap the createTransactor function into an IO monad
   * so that it can be used in a monad sequence
   */
  def createIoTransactor(dbConf: IO[DbConfig]): 
    IO[Transactor.Aux[IO,Unit]] = 
      dbConf.map(conf => createTransactor(conf))
}

