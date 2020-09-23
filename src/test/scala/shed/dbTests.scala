import doobie._
import doobie.implicits._
import cats._
import cats.data._
import cats.effect._
import cats.implicits._

import org.scalatest._
import org.scalatest.matchers.should.Matchers

import shed.Setup
import shed.config._
import shed.db._
import shed.models._


class DbTestScalaCheck extends funsuite.AnyFunSuite with Matchers with doobie.scalatest.IOChecker with BeforeAndAfter{

  val transactor = Config.createIoTransactor(Config.myDbConfig).
                      unsafeRunSync()
  before { 
    Setup.runDropTables.unsafeRunSync()
    Setup.runCreateTables.unsafeRunSync()
  }
  after {
    Setup.runDropTables.unsafeRunSync()
  }

  ignore("Checking queries") {
    check(Queries.createItemTable)
    check(Queries.createTagTable)
    check(Queries.createItem2TagTable)
    check(Queries.dropItemTable)
    check(Queries.dropTagTable)
    check(Queries.dropItem2TagTable)
    check(Queries.getItems)
    check(Queries.getItemByName("tent"))
    check(Queries.getItemByTag("tag"))
    check(Queries.getItemByTagList(NonEmptyList.of("tag1", "tag2", "tag3")))
    check(Queries.insertItem("test","testdescription"))
    check(Queries.removeItem(12))
    check(Queries.removeItemTags(12))
    check(Queries.addTag2Item(1, 2))
    check(Queries.removeTagFromItem(1,2))
    check(Queries.getAllTags)
    check(Queries.insertTagList)
      
  }

  val testShed = new GearShed(transactor)
  val tent = Item(None, "tent", Some("super light tent"))
  val tagList = List("Tag1", "Tag2")
  val tagList2 = List("Tag2", "Tag3")

  ignore("Can insert an item") {
    val tentCheck = testShed.insertItem(tent).unsafeRunSync()
    assert(tentCheck.name == "tent" &&
           tentCheck.description.getOrElse("") == "super light tent")

  }

  ignore("Can get items list") {
    //val itemListCheck = testShed.getItems.unsafeRunSync()
    val tentCheck = testShed.insertItem(tent).unsafeRunSync()
    assertResult(List(Item(Some(1), "tent", Some("super light tent")))) {
      testShed.getItems.unsafeRunSync()
    }
    assertResult(List(Item(Some(1), "tent", Some("super light tent")))) {
      testShed.getItemByName("tent").unsafeRunSync()
    }
    assertResult(List()) {
      testShed.getItemByName("Not available").unsafeRunSync()
    }
  }

  ignore("Insert tags") {
    assertResult(2) {
      testShed.insertTagList(tagList).unsafeRunSync()
    }
    assertResult(List(shed.models.Tag(1,"Tag1"),shed.models.Tag(2,"Tag2"))) {
      testShed.getAllTags.unsafeRunSync()
    }
    assertResult(1) {
      testShed.insertTagList(tagList2).unsafeRunSync()
    }
  }

  ignore("Associating Items and Tags") {
    val tentCheck = testShed.insertItem(tent).unsafeRunSync()
    val tagCount = testShed.insertTagList(tagList).unsafeRunSync()
    assertResult(1) {
      testShed.addTag2Item(1,1).unsafeRunSync()
    }
    assertResult(1) {
      testShed.addTag2Item(1,2).unsafeRunSync()
    }
    assertResult(List(Item(Some(1), "tent", Some("super light tent")))) {
      testShed.getItemByTag("Tag1").unsafeRunSync()
    }
    assertResult(List(Item(Some(1), "tent", Some("super light tent")))) {
      testShed.getItemByTag("Tag2").unsafeRunSync()
    }
    assertResult(List()) {
      testShed.getItemByTag("Tag3").unsafeRunSync()
    }
    assertResult(List(Item(Some(1), "tent", Some("super light tent")))) {
      testShed.getItemByTagList(NonEmptyList.of("Tag1", "Tag2")).unsafeRunSync()
    }
    assertResult(List(Item(Some(1), "tent", Some("super light tent")))) {
      testShed.getItemByTagList(NonEmptyList.of("Tag2", "Tag3")).unsafeRunSync()
    }
    /* Test removing tags */
    assertResult(List()) {
      val nRemoved = testShed.removeTagFromItem(1,2).unsafeRunSync()
      testShed.getItemByTag("Tag2").unsafeRunSync()
    }
    assertResult(List(Item(Some(1), "tent", Some("super light tent")))) {
      testShed.getItemByTag("Tag1").unsafeRunSync()
    }
  } 
   /* Test removing items */

  ignore("Removing Items and Tags") {
    val tentCheck = testShed.insertItem(tent).unsafeRunSync()
    val tagCount = testShed.insertTagList(tagList).unsafeRunSync()
    testShed.addTag2Item(1,1).unsafeRunSync()
    testShed.addTag2Item(1,2).unsafeRunSync()
    assertResult(3) {
      testShed.removeItem(1).unsafeRunSync()
    }
    assertResult(List()) {
      testShed.getItems.unsafeRunSync()
    }
  }
}
