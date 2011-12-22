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

  /**
   * Gets the object from the database that has the specified [[uk.co.randomcoding.partsdb.code.id.Identifier]].
   *
   * @param idFieldName The name of the field that maps to the [[uk.co.randomcoding.partsdb.code.id.Identifier]].
   * @param id The [[uk.co.randomcoding.partsdb.code.id.Identifier]] already assigned to the object
   * @return The object of type `TYPE` that has the [[uk.co.randomcoding.partsdb.code.id.Identifier]] of `id`
   */
  def getOne[TYPE <: AnyRef](idFieldName: String, id: Identifier)(implicit mf: Manifest[TYPE]): Option[TYPE] = {
    collection.findOne(queryOne(idFieldName, id)).toList match {
      case Nil => None
      case head :: Nil => Some(convertFromMongoDbObject(head))
      case head :: tail :: Nil => {
        error("Query for single item [%s] returned multiple objects. Returning first result only")
        Some(convertFromMongoDbObject(head))
      }
    }
  }

  /**
   * Gets all the instances of a type of object from the database
   *
   * @param idFieldName The name of the id field in the object.
   * @return All instances of the objects of type `TYPE` from the database.
   */
  def getAll[TYPE <: AnyRef](idFieldName: String)(implicit mf: Manifest[TYPE]): List[TYPE] = {
    for (result <- collection.find(queryAll(idFieldName)).toList) yield {
      convertFromMongoDbObject(result)
    }
  }

  /**
   * Returns the results that match the provided query
   */
  def getMatching[TYPE](query: MongoDBObject)(implicit mf: Manifest[TYPE]): List[TYPE] = {
    for (result <- collection.find(query).toList) yield {
      convertFromMongoDbObject(result)
    }
  }
}