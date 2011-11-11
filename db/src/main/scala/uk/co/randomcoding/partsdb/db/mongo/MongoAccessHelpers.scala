/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import com.mongodb.casbah.Imports._
import MongoConverters._
import net.liftweb.common.Logger
import uk.co.randomcoding.partsdb.core.id.Identifiable

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object MongoAccessHelpers extends Logger {

  /**
   * Checks the given collection does not contain an object with the [[uk.co.randomcoding.partsdb.core.id.Identifier]] of the provided object `(t)`.
   *
   * Can be used to ensure there is not an identifier duplication before adding an object
   *
   * @return `true` If the [[uk.co.randomcoding.partsdb.core.id.Identifier]] from `t` is not present in the collection
   */
  def idNotInDb[TYPE <: Identifiable](t: TYPE, collection: MongoCollection)(implicit mf: Manifest[TYPE]): Boolean = {
    idNotInDb(collection, objectIdInDbQuery(t))
  }

  /**
   * Checks the given collection contains an object with the [[uk.co.randomcoding.partsdb.core.id.Identifier]] of the provided object `(t)`.
   *
   * Can be used to check if an object has been added to the db by its id only
   *
   * @return `true` If the [[uk.co.randomcoding.partsdb.core.id.Identifier]] from `t` is present in the collection
   */
  def idIsInDb[TYPE <: Identifiable](t: TYPE, collection: MongoCollection)(implicit mf: Manifest[TYPE]): Boolean = {
    idIsInDb(collection, objectIdInDbQuery(t))
  }

  /**
   * Gets the MongoDBObject that represents the item with the given id.
   *
   * @param item The
   */
  def getDbObject[TYPE <: Identifiable](item: TYPE, collection: MongoCollection)(implicit mf: Manifest[TYPE]): MongoDBObject = {
    val objectWithIdentifier = collection.findOne(objectIdInDbQuery(item)).getOrElse(DBObject.empty)
    // This seems to be required to enable a correct implicit conversion to MongoDBObject
    objectWithIdentifier
  }

  /*
   * Helper conversions and functions for the above functions
   */

  private val idNotInDb = (collection: MongoCollection, idQuery: MongoDBObject) => {
    debug("Checking for %s in the db".format(idQuery))
    val notFound = collection.findOne(idQuery).isEmpty
    debug("Found: %S".format(!notFound))
    notFound
  }

  private val idIsInDb = (collection: MongoCollection, idQuery: MongoDBObject) => {
    debug("Checking for %s in the db".format(idQuery))
    val found = collection.findOne(idQuery).isDefined
    debug("Found: %S".format(found))
    found
  }

  private def objectIdInDbQuery(item: Identifiable): MongoDBObject = MongoDBObject(item.identifierFieldName -> MongoDBObject("id" -> item.id))

  private implicit def idEntryToMongoDBIdentifierQueryObject(idEntry: (String, AnyRef)): MongoDBObject = {
    val idQuery = MongoDBObject(idEntry._1 -> MongoDBObject("id" -> idEntry._2.asInstanceOf[BasicDBObject].as[Long]("id")))
    debug("Created idQuery: %s".format(idQuery))
    idQuery
  }
}