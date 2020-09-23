package shed

import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import cats._
import cats.data._
import cats.effect._
import cats.implicits._

import shed.db._
import shed.config._

object Setup {
  /* A set of functions to create and remove the 
   * tables for this project 
   *
   * The functions are intended to be run from the 
   * REPL, but could be wrapped in a command line
   * application. 
   */

  val createTables: ConnectionIO[Int] = (
    Queries.createItemTable.run,
    Queries.createTagTable.run,
    Queries.createItem2TagTable.run
  ).mapN(_ + _ + _)

  val altCreateTables: IO[Int] =
    Config.createIoTransactor(Config.myDbConfig).flatMap(xa =>
        createTables.transact(xa))

  val runCreateTables: IO[Unit] = 
    for {
      xa <- Config.createIoTransactor(Config.myDbConfig)
      _ <- Queries.createItemTable.run.transact(xa)
      _ <- Queries.createTagTable.run.transact(xa)
      _ <- Queries.createItem2TagTable.run.transact(xa)
    } yield () 

  val runDropTables: IO[Unit] = 
    for {
      xa <- Config.createIoTransactor(Config.myDbConfig)
      _ <- Queries.dropItemTable.run.transact(xa)
      _ <- Queries.dropTagTable.run.transact(xa)
      _ <- Queries.dropItem2TagTable.run.transact(xa)
    } yield ()

  // ran Setup.runCreateTables.unsafeRunSync()
}
