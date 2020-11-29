package shed

import cats.effect._
import cats.data._              // NonEmptyList
import cats.implicits._
import io.circe._
import io.circe.literal._
import io.circe.generic.auto._  // for encoder/decoder
import io.circe.syntax._        // for asJson, and as[Class]

import org.http4s.circe._
import org.http4s.circe.CirceEntityDecoder._ 
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._

import shed.models._

class GearShedService(actions: GearShed) {
  type TagLabel = String

  implicit val itemDecoder = jsonOf[IO, Item]
  implicit val tagDecoder = jsonOf[IO, Tag]

  def list2JsonOutput(list: List[Item]): IO[Response[IO]] =
    list match {
      case Nil => NoContent()
      case _ => Ok(list.asJson)
    }


  val routes = HttpRoutes.of[IO] {
    case GET -> Root / "items" => 
      actions.getItems.flatMap( items => 
        list2JsonOutput(items) 
      )
    case GET -> Root / "items" / name =>
      actions.getItemByName(name).flatMap( items => 
        list2JsonOutput(items)
      )

    case GET -> Root / "itemsByTag" / label =>
      actions.getItemByTag(label).flatMap( items => 
        list2JsonOutput(items)
      )

    case req @ GET -> Root / "itemsByTagList" => 
      for {
        tagList <- req.as[List[String]]
        resp <- NonEmptyList.fromList(tagList) match { 
              case None => NoContent()
              case Some(nonEmptyList) => 
                 actions.getItemByTagList(nonEmptyList).flatMap {
                    items => list2JsonOutput(items)
                 }
               }
      } yield resp

    case req @ PUT -> Root / "insertItem" => 
      for {
        item <- req.as[Item]
        insertedItem <-  actions.insertItem(item)
        response <- Created(insertedItem.asJson)
      } yield response    

    case POST -> Root / "removeItem" / IntVar(itemId) => 
      actions.removeItem(itemId).flatMap(n => Ok(n.asJson))
    case POST -> Root / "addTag2Item" / IntVar(itemId) / IntVar(tagId) =>
      actions.addTag2Item(itemId, tagId).flatMap{ nRecords => 
        nRecords match {
          case 0 => NoContent()
          case _ => Created(nRecords.asJson)
        }
      }

    case POST -> Root / "removeTagFromItem" / IntVar(itemId) / IntVar(tagId) => 
      actions.removeTagFromItem(itemId,tagId).flatMap(n => Ok(n.asJson))

    case GET -> Root / "getTags" => 
      actions.getAllTags.flatMap{ tagList => 
        tagList match {
          case Nil => NoContent()
          case _ => Ok(tagList.asJson)
        }
      }
      
    case req @ PUT -> Root / "insertTagList" => 
      for {
        tagList <- req.as[List[String]]
        insertedTags <-  actions.insertTagList(tagList)
        response <- Created(insertedTags.asJson)
      } yield response

  }.orNotFound
}
