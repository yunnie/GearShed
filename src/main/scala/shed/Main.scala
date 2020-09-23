import cats.effect._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze._
import scala.concurrent.ExecutionContext.global

// Import the shed packages
import shed._
import shed.config.Config
import shed.models._
import shed.Setup

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {

  // Configure the service
    for {
      transactor <- Config.createIoTransactor(Config.myDbConfig)
      serverConfig <- Config.myServerConfig
      _ <- Setup.runCreateTables
      exitcode <- BlazeServerBuilder[IO](global).
        bindHttp(serverConfig.port, serverConfig.host).
        withHttpApp((new GearShedService( new GearShed(transactor))).routes).
        serve.
        compile.
        drain.
        as(ExitCode.Success)
    } yield exitcode

      
  }
}
