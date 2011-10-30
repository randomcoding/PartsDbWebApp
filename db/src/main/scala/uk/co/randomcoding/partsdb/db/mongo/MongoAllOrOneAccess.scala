/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import com.mongodb.casbah.Imports._
import uk.co.randomcoding.partsdb.db.mongo.MongoConverters._
import uk.co.randomcoding.partsdb.core.id.Identifier
import net.liftweb.common.Logger

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
trait MongoAllOrOneAccess extends Logger {

  val collection: MongoCollection

  private implicit def idToMongo(id: Identifier): MongoDBObject = {
    id.identifierType match {
      case "" => MongoDBObject("id" -> id.uniqueId)
      case idType => MongoDBObject("id" -> id.uniqueId,
        "type" -> idType)
    }
  }

  /**
   * Define a function that generates a query to get a specific item from the db
   */
  val queryOne = (idFieldName: String, idFieldValue: Identifier) => {
    val mondoIDentifier: MongoDBObject = idFieldValue
    MongoDBObject(idFieldName -> idFieldValue)
  }

  /**
   * A function that generates a query that looks for all objects that have a specific id field.
   * As all objects have a field `addresId`, `partId` etc that is its unique identifier this
   * only requires the identifier exist by name.
   */
  val queryAll = (idFieldName: String) => idFieldName $exists true

  def getOne[T <: AnyRef](query: MongoDBObject)(implicit mf: Manifest[T]): Option[T] = {
    collection.findOne(query).toList match {
      case Nil => None
      case head :: Nil => Some(convertFromMongoDbObject(head))
      case head :: tail :: Nil => {
        error("Query for single item [%s] returned multiple objects. Returning first result only")
        Some(convertFromMongoDbObject(head))
      }
    }
  }

  def getAll[T <: AnyRef](query: MongoDBObject)(implicit mf: Manifest[T]): List[T] = {
    for (result <- collection.find(query).toList) yield {
      convertFromMongoDbObject(result)
    }
  }
}