/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import com.mongodb.casbah.Imports._
import uk.co.randomcoding.partsdb.db.mongo.MongoConverters._
import uk.co.randomcoding.partsdb.core.id._
import net.liftweb.common.Logger

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
trait MongoAllOrOneAccess extends Logger {

  /**
   * The '''`MongoCollection`''' that this trait will access
   */
  val collection: MongoCollection

  /**
   * Define a function that generates a query to get a specific item from the db
   */
  private val queryOne: (String, Identifier) => MongoDBObject = (idFieldName: String, idFieldValue: Identifier) => {
    val mongoIdentifier = MongoDBObject("id" -> idFieldValue.id)
    MongoDBObject(idFieldName -> mongoIdentifier)
  }

  /**
   * A function that generates a query that looks for all objects that have a specific id field.
   * As all objects have a field `addresId`, `partId` etc that is its unique identifier this
   * only requires the identifier exist by name.
   */
  private val queryAll = (idFieldName: String) => idFieldName $exists true

  def getOne[T <: AnyRef](idFieldName: String, id: Identifier)(implicit mf: Manifest[T]): Option[T] = {
    collection.findOne(queryOne(idFieldName, id)).toList match {
      case Nil => None
      case head :: Nil => Some(convertFromMongoDbObject(head))
      case head :: tail :: Nil => {
        error("Query for single item [%s] returned multiple objects. Returning first result only")
        Some(convertFromMongoDbObject(head))
      }
    }
  }

  def getAll[T <: AnyRef](idFieldName: String)(implicit mf: Manifest[T]): List[T] = {
    for (result <- collection.find(queryAll(idFieldName)).toList) yield {
      convertFromMongoDbObject(result)
    }
  }
}