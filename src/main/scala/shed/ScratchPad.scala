import io.circe._
import io.circe.config.parser
import io.circe.generic.auto._
import io.circe.syntax._

import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.client.dsl.io._
import org.http4s.implicits._
import org.http4s.syntax._

import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import cats._
import cats.data._
import cats.effect._
import cats.implicits._

import shed.GearShedService
import shed.models._
import shed.config._
import shed.Setup


object Pad {
  type TagLabel = String
  val item1 = Item(None, "item1", Some("description1"))
  val item2 = Item(None, "item2", Some("description2"))
  val tag1 = Tag(1, "tag1")
  val tag2 = Tag(2, "tag2")
  val tagList = List("tag1", "tag2")
  val tagListJson = tagList.asJson
  val item1json = item1.asJson
  val item2json = item2.asJson
 
  val xa = Config.createIoTransactor(Config.myDbConfig).unsafeRunSync()

  val start: IO[Unit] = for {
    _ <- Setup.runDropTables
    _ <- Setup.runCreateTables
  } yield ()

  val cleanup: IO[Unit] = Setup.runDropTables

  val ShedServices = new GearShedService(new GearShed(xa))

  val insertItem1 = PUT(item1json, uri"/insertItem").unsafeRunSync()

  val insertItem2 = Request[IO](Method.PUT, uri"/insertItem").withEntity(item2.asJson) 

  val getListItems = Request[IO](Method.GET, uri"/items")

  val ioInsertItem1 = ShedServices.routes.run(insertItem1)

  val ioInsertItem2 = ShedServices.routes.run(insertItem2)

  val io = ShedServices.routes.run(getListItems)

  def readFile(file: String): IO[List[String]] = 
    IO(scala.io.Source.fromFile(file)).bracket { 
      source =>
        IO(source.getLines().toList)
      } { 
      source => 
        IO(source.close())
      }
 
  def writeFile(file: String, output: String): IO[Unit] =
    IO(new java.io.PrintWriter(new java.io.File(file))).bracket {
      writer => IO(writer.write(output))
    } {
      writer => IO(writer.close())
    }

  def readFileAlt(file: String): IO[List[String]] =
    for {
      source <- IO(scala.io.Source.fromFile(file))
      lines <- IO(source.getLines().toList)
      _ <- IO(source.close())
    } yield lines

  def writeFileAlt(file: String, output: String): IO[Unit] = 
    for {
      writer <- IO(new java.io.PrintWriter(new java.io.File(file)))
      _ <- IO(writer.write(output))
      _ <- IO(writer.close())
    } yield ()
}
