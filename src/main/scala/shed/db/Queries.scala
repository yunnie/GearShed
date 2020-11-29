package shed.db

import doobie._
import doobie.implicits._

import cats._
import cats.data._
import cats.implicits._

import shed.models._

object Queries {
  type TagLabel = String 

  def createItemTable: Update0 = 
    sql"""
      CREATE TABLE IF NOT EXISTS Item (
        id           serial,
        name         varchar(35) NOT NULL,
        description  text 
      )""".update 

  def dropItemTable: Update0 = 
    sql"""
      DROP TABLE IF EXISTS Item
      """.update

  def createTagTable: Update0 = 
    sql"""
      CREATE TABLE IF NOT EXISTS Tag (
        id       serial,
        label    varchar(20) NOT NULL
      )""".update

  def dropTagTable: Update0 =
    sql"""
      DROP TABLE IF EXISTS Tag
      """.update

  def createItem2TagTable: Update0 = 
    sql"""
      CREATE TABLE IF NOT EXISTS Item2Tag (
        item_id  integer,
        tag_id   integer
      )""".update

  def dropItem2TagTable: Update0 =
    sql"""
      DROP TABLE IF EXISTS Item2Tag 
      """.update


  def getItems: Query0[Item] = 
    sql"""
      SELECT id, name, description
      FROM Item
      """.query[Item]
      
  def getItemByName(name: String): Query0[Item] = 
    sql"""
      SElECT id, name, description
      FROM Item
      WHERE name = $name
      """.query[Item]

  def getItemByTag(tag: String): Query0[Item] = 
    sql"""
      SELECT distinct i.id, i.name, i.description
      FROM Item i,
           Item2Tag i2t,
           Tag t
      WHERE i.id = i2t.item_id 
      AND   i2t.tag_id = t.id
      AND   t.label = $tag
      """.query[Item]

  def getItemByTagList(tags: NonEmptyList[String]): Query0[Item] = { 
    val q = sql"""
      SELECT distinct i.id, i.name, i.description
      FROM Item i,
           Item2Tag i2t,
           Tag t
      WHERE i.id = i2t.item_id 
      AND   i2t.tag_id = t.id
      AND   """ ++ Fragments.in(fr"t.label", tags)
    q.query[Item]
  }

  def insertItem(name: String, description: String): Update0 = 
    sql"""
      INSERT into Item (name, description)
      VALUES ($name,$description)
      """.update
  
  def removeItem(itemId: Int): Update0 =
    sql"""
      DELETE FROM Item
      WHERE id = $itemId
      """.update
 
  def removeItemTags(itemId: Int): Update0 = 
    sql"""
      DELETE FROM Item2Tag
      WHERE item_id = $itemId
      """.update
      
  def addTag2Item(itemId: Int, tagId: Int): Update0 = 
    sql"""
      INSERT into Item2Tag (item_id, tag_id)
      VALUES ($itemId, $tagId)
      """.update
  
  def removeTagFromItem(itemId: Int, tagId: Int): Update0 = 
    sql"""
      DELETE from Item2Tag
      WHERE item_id = $itemId AND tag_id = $tagId
      """.update
  
  val getAllTags: Query0[Tag] = 
    sql"""
      SELECT id, label 
      FROM tag
      """.query[Tag]
  
  def insertTagList: Update[TagLabel] = {
    val query = "insert into tag (label) values (?)"
    Update[TagLabel](query)
  }
}
