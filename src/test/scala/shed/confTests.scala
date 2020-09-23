import doobie._
import doobie.implicits._
import cats._
import cats.data._
import cats.effect._
import cats.implicits._

import org.scalatest._
import org.scalatest.matchers.should.Matchers
import shed.config._
class ConfTestScalaCheck extends funsuite.AnyFunSuite {

  // Import the transactor
  test("The DataBase configuration should be an IO[DbConfig]") {
    assert(Config.myDbConfig.unsafeRunSync() == 
      DbConfig("org.postgresql.Driver", 
        "jdbc:postgresql:shed",
        "postgres",
        "password"))
  }

  test("The Server configuration should be an IO[ServerConfig]") {
    assert(Config.myServerConfig.unsafeRunSync() ==
      ServerConfig(
        "localhost",
        8080
      )
    )
  }
}
