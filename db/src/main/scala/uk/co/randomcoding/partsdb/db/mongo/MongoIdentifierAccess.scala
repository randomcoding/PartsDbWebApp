/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import com.mongodb.casbah.Imports._
import uk.co.randomcoding.partsdb.db.mongo.MongoConverters._
import uk.co.randomcoding.partsdb.core.id.Identifier

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
trait MongoIdentifierAccess {
  val collection: MongoCollection

  private val incrementUniqueId = (currentId: Long) => {
    val newIdObject = MongoDBObject("uniqueId" -> (currentId + 1))
    val findIdObjectQuery = "uniqueId" $exists true
    collection.findOne(findIdObjectQuery) match {
      case None => collection += newIdObject
      case _ => collection.findAndModify(findIdObjectQuery, newIdObject)
    }
  }

  private val idQuery = "uniqueId" $exists true

  /**
   * Gets the next value for the unique id.
   *
   * This also increments the current value that is stored in the database
   */
  def nextId(): Long = {
    val findOneQuery = collection.findOne(idQuery)
    val idValue = findOneQuery match {
      case None => {
        0
      }
      case Some(v) => {
        v.as[Long]("uniqueId")
      }
    }
    incrementUniqueId(idValue)
    idValue
  }
}