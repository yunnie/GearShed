import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._

import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.client.dsl.io._
import org.http4s.implicits._
import org.http4s.syntax._

import cats._
import cats.data._
import cats.effect._
import cats.implicits._

import org.scalatest._
import org.scalatest.matchers.should.Matchers

import shed.GearShedService
import shed.models._
import shed.config._
import shed.Setup



class ServiceTestScalaCheck extends funsuite.AnyFunSuite with Matchers with BeforeAndAfterAll {

  val transactor = Config.createIoTransactor(Config.myDbConfig).unsafeRunSync()
  val setup: IO[Unit] = for {
    _ <- Setup.runDropTables
    _ <- Setup.runCreateTables
  } yield ()

  val cleanup: IO[Unit] = Setup.runDropTables

  override def beforeAll(): Unit = {
    setup.unsafeRunSync()
  }

  override def afterAll(): Unit = {
    cleanup.unsafeRunSync()
  }

  val ShedServices = new GearShedService( new GearShed(transactor))

  type TagLabel = String
  val item1 = Item(None, "item1", Some("description1"))
  val item2 = Item(None, "item2", Some("description2"))
  val tag1 = shed.models.Tag(1, "tag1")
  val tag2 = shed.models.Tag(2, "tag2")
  val tag3 = shed.models.Tag(3, "tag3")

  test("Can insert item") {
    val insertItem1 = Request[IO](Method.PUT, uri"/insertItem").
      withEntity(item1.asJson)
    val response = ShedServices.routes.run(insertItem1).
      unsafeRunSync()

    assertResult(Item(Some(1), "item1", Some("description1")).asJson) {
      response.as[Json].unsafeRunSync()
    }
  }

  test("Query list of items") {
    val insertItem2 = Request[IO](Method.PUT, uri"/insertItem").
      withEntity(item2.asJson)

    val insertResponse = ShedServices.routes.run(insertItem2).
      unsafeRunSync()

    val getItemsRequest = Request[IO](Method.GET, uri"/items")
     
    val listResponse = ShedServices.routes.run(getItemsRequest).
      unsafeRunSync()
   
    val expected = List(Item(Some(1), "item1", Some("description1")),
                        Item(Some(2), "item2", Some("description2"))).asJson
    assertResult(expected) {
      listResponse.as[Json].unsafeRunSync()
    }
  }

  test("Get item by name") {
    val getItemByNameReq = Request[IO](Method.GET, uri"/items/item1")
    val listResponse = ShedServices.routes.run(getItemByNameReq).
      unsafeRunSync()
    val expected = List(Item(Some(1), "item1", Some("description1"))).asJson

    assertResult(expected) {
      listResponse.as[Json].unsafeRunSync()
    }
  }

  test("Get item by name but name does not exist") {
    val getItemByNameReq = Request[IO](Method.GET, uri"/items/item3")
    val listResponse = ShedServices.routes.run(getItemByNameReq).
      unsafeRunSync()

    assertResult(Status.NoContent) {
      listResponse.status
    }
  }

  test("Insert tag list") {
    val tagList = List("tag1", "tag2", "tag3")
    val insertTagListReq = Request[IO](Method.PUT, uri"/insertTagList").
      withEntity(tagList.asJson)
    val insertTagListResponse = ShedServices.routes.run(insertTagListReq).unsafeRunSync()
    assertResult(3.asJson) {
      insertTagListResponse.as[Json].unsafeRunSync()
    }
  }

  test("Get all tags") {
    val getTagList = Request[IO](Method.GET, uri"/getTags")
    val tagListResponse = ShedServices.routes.run(getTagList).
      unsafeRunSync()
    
    val expected = List(shed.models.Tag(1, "tag1"), 
                        shed.models.Tag(2, "tag2"), 
                        shed.models.Tag(3, "tag3")).asJson
    assertResult(expected) {

      tagListResponse.as[Json].unsafeRunSync()
    }
  }

  /* Add tag2item test */
  test("add tag to item") {
    val addTag2ItemReq = Request[IO](Method.POST, uri"/addTag2Item/1/1")
    val tag2ItemResponse = ShedServices.routes.run(addTag2ItemReq).
      unsafeRunSync()

    assertResult(1.asJson) {
      tag2ItemResponse.as[Json].unsafeRunSync()
    }
  }

  /* Add itemByTag search test */
  test("get item by tag") {
    val getItemByTagReq = Request[IO](Method.GET, uri"/itemsByTag/tag1")
    val listResponse = ShedServices.routes.run(getItemByTagReq).unsafeRunSync()
    val expected = List(Item(Some(1), "item1", Some("description1"))).asJson

    assertResult(expected){
      listResponse.as[Json].unsafeRunSync()
    }
  }

  test("Get item by tag but tag does not exist") {
    val getItemByTagReq = Request[IO](Method.GET, uri"/items/item3")
    val listResponse = ShedServices.routes.run(getItemByTagReq).
      unsafeRunSync()

    assertResult(Status.NoContent) {
      listResponse.status
    }
  }

  /* Add test for Item by Tag List */
  test("Get items by a list of tags") {
    val tagList = List("tag1", "tag2", "tag3")
    val itemListReq = Request[IO](Method.GET, uri"/itemsByTagList").
      withEntity(tagList.asJson)
    val listResponse = ShedServices.routes.run(itemListReq).
      unsafeRunSync()

    val expected = List(Item(Some(1), "item1", Some("description1"))).asJson
    assertResult(expected) {
      listResponse.as[Json].unsafeRunSync()
    }
  }

  /* Remove tag from item */
  test("Remove tag from item") {
    val removeTagRequest = Request[IO](Method.POST, uri"/removeTagFromItem/1/1")
    val removeResponse = ShedServices.routes.run(removeTagRequest).
      unsafeRunSync()

    // Removes tag1 from tag2item table 
    assertResult(1.asJson) {
      removeResponse.as[Json].unsafeRunSync()
    }
  }

  /* Remove item */
  test("Remove item from table") { 
    val removeTagRequest = Request[IO](Method.POST, uri"/removeItem/1")
    val removeResponse = ShedServices.routes.run(removeTagRequest).
      unsafeRunSync()

    // Removes item1 from item table
    // At this point there are no tags
    assertResult(1.asJson) {
      removeResponse.as[Json].unsafeRunSync()
    }
  }

}
