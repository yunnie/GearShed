package shed.models

import cats.effect.IO
import cats.implicits._
import cats.data._
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor

import shed.db.Queries


case class Item(id: Option[Int], name: String, description: Option[String])
case class Tag(id: Int, label: String)
case class InventoryItem(item: Item, tags: List[Tag])
case object ItemNotFoundError

class GearShed(xa: Transactor[IO]) {
  type TagLabel = String
  val getItems: IO[List[Item]] = 
    Queries.getItems.to[List].transact(xa)

  /* Need to decide how to querie? Do I pass in a name?
   * Do I pass in a json? Return a list because
   * there could be several items with the same name
   */
  def getItemByName(name: String): IO[List[Item]] = 
    Queries.getItemByName(name).to[List].transact(xa)

  def getItemByTag(label: String): IO[List[Item]] = 
    Queries.getItemByTag(label).to[List].transact(xa)

  def getItemByTagList(tags: NonEmptyList[String]): IO[List[Item]] = 
    // Consider using NonEmptyList.fromList(listname)
    // Rturns a Option[NonEmptyList[A]]
    
    Queries.getItemByTagList(tags).to[List].transact(xa)

  def insertItem(item: Item): IO[Item] = 
    Queries.insertItem(item.name, item.description.getOrElse("")).
      withUniqueGeneratedKeys[Int]("id").
      transact(xa).
      map { id => item.copy(id = Some(id))}

  def removeItem(itemId: Int): IO[Int] = 
  /* Should probably be done by id since that is the only 
   * thing to be unique 
   * Should also remove the Item2Tag records with the same item_id
   * */
    for {
      nItemDeleted <- Queries.removeItem(itemId).run.transact(xa)
      nTagsDeleted <- Queries.removeItemTags(itemId).run.transact(xa)
    } yield nItemDeleted + nTagsDeleted

  def addTag2Item(itemId: Int, tagId: Int): IO[Int] = 
    Queries.addTag2Item(itemId, tagId).run.transact(xa)

  def removeTagFromItem(itemId: Int, tagId: Int): IO[Int] = 
    Queries.removeTagFromItem(itemId, tagId).run.transact(xa)

  val getAllTags: IO[List[Tag]] = 
    Queries.getAllTags.to[List].transact(xa)

  def insertTagList(tagList: List[TagLabel]): IO[Int] =
    /* Need to modify to make sure the tag list is unique */
    for {
      existingTags <- getAllTags
      /* Convert the list of Tag(id, label) to a list of TagLabel */
      existingTagSet <- IO(existingTags.map(tag => tag.label))
      /* Find the set difference from the new tags to the existing tags
       * to see which tags need to be inserted, so that the tags are unique
       * Can probably use POSTGRES constraint to handle this
       */ 
      newTags      <- IO((tagList.toSet diff existingTagSet.toSet).toList)
      /* Finally, at long last, run the query */
      i <- Queries.insertTagList.updateMany(newTags).
             transact(xa)
    } yield i

}
