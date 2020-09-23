package shed

import cats.effect._
import cats.data._ // NonEmptyList
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


/* To do: 
 * Basically write in the services. 
 * Write in the Routes and responses
 * Note that org.http4s.dsl.io is an object that extends Http4sDsl[IO]
 */

class GearShedService(actions: GearShed) {
  type TagLabel = String
  // Json Decoders 
  implicit val itemDecoder = jsonOf[IO, Item]
  implicit val tagDecoder = jsonOf[IO, Tag]

  // Cound not find implicit value parameter for encoder: io
  // io.circe.Encoder[A]
  def list2JsonOutput(list: List[Item]): IO[Response[IO]] =
    list match {
      case Nil => NoContent()
      case _ => Ok(list.asJson)
      /* case _ => Ok(list.map(_.asJson.noSpaces)) 
       * Connot convert from List[String] to an Entity
       * because no EntityEncoder[IO,List[String]]
       * could be found. This means that 
       * Ok(List(json1, json2,...,jsonn))
       * and Ok doesn't know how to proess the list
       * of json strings.
       * changing the line to
       * case _ => Ok(list.asJson) 
       * worked, as it now relies on the io.circe.generic.auto._
       * library
       */
    }


  val routes = HttpRoutes.of[IO] {
  /* Get List of Items 
   * Parameters: None
   * Return: IO[List[Item]]
   * */
    case GET -> Root / "items" => 
      /* IO is not a case object so can't be used 
       * for pattern matching */
      actions.getItems.flatMap( items => 
        list2JsonOutput(items) 
        )
      /* Note that Ok() returns an IO[Response[IO]] */  
      /* Should we add a content header? */

  /* Get Item by Name 
   * Parameters: name: String
   * Return: IO[List[Item]]
   * */
     case GET -> Root / "items" / name =>
       actions.getItemByName(name).flatMap( items => 
         list2JsonOutput(items)
       )

  /* Get Item by Tag Label 
   * Parameters: label: String
   * Return: IO[List[Item]]
   * */
     case GET -> Root / "itemsByTag" / label =>
       actions.getItemByTag(label).flatMap( items => 
         list2JsonOutput(items)
       )

  /* Get Item by Tag Label List 
   * Parameters: tags: NonEmpytList[String]
   * Return: IO[List[Item]]
   * */
  // makes the most sense to pass the list as JSON
     case req @ GET -> Root / "itemsByTagList" => 
       for {
         // missing an entity encoder for List[String]
         // import org.http4s.circe.CirceEntityDecoder._ 
         tagList <- req.as[List[String]]
         resp <- NonEmptyList.fromList(tagList) match { 
           // NonEmptyList.fromList returns an Option 
              case None => NoContent()
              case Some(nonEmptyList) => 
                 actions.getItemByTagList(nonEmptyList).flatMap {
                    items => list2JsonOutput(items)
                 }
               }
       } yield resp
  /* Insert Item  
   * Paramater: item: Item
   * Return: IO[Item]
   * */
   // Cannot decode into a value of type shed.models.Item
   // because no EntityDecoder[cats.effect.IO, shed.models.Item]
   // instance could be found.
   // try import ort.http4s.circe._
   // 
   // Just checked this on the ScratchPad and it works
     case req @ PUT -> Root / "insertItem" => 
       for {
         item <- req.as[Item]
         insertedItem <-  actions.insertItem(item)
         response <- Created(insertedItem.asJson)
       } yield response    
       /* I was expecting req.as[Item]: Either[Error, Item]
        * but req.as[Item] seems to be an IO[Item] 
        * Created is a status, for which there is an implicit
        * http4sCreatedSyntax, which returns a CreatedOps.
        * see org.http4s.dsl.impl under Resposnes
        */


  /* remove Item 
   * Parameter: itemId: Int
   * Return> IO[Int] (number of records removed)
   * */
     case POST -> Root / "removeItem" / IntVar(itemId) => 
       actions.removeItem(itemId).flatMap(n => Ok(n.asJson))

  /* add tag to an item 
   * Parameter: itemId: Int, tagId: Int):
   * Return: IO[Int] (number of records added)
   * */
     case POST -> Root / "addTag2Item" / IntVar(itemId) / IntVar(tagId) =>
       actions.addTag2Item(itemId, tagId).flatMap{ nRecords => 
         nRecords match {
           case 0 => NoContent()
           case _ => Created(nRecords.asJson)
         }
       }

  /* Remove tag from item */
     case POST -> Root / "removeTagFromItem" / IntVar(itemId) / IntVar(tagId) => 
       actions.removeTagFromItem(itemId,tagId).flatMap(n => Ok(n.asJson))

  /* get all the tags */
     case GET -> Root / "getTags" => 
       actions.getAllTags.flatMap{ tagList => 
         tagList match {
           case Nil => NoContent()
           case _ => Ok(tagList.asJson)
         }
       }


  /* insert tag list */
     case req @ PUT -> Root / "insertTagList" => 
       for {
         tagList <- req.as[List[String]]
         insertedTags <-  actions.insertTagList(tagList)
         response <- Created(insertedTags.asJson)
       } yield response

  }.orNotFound
}
