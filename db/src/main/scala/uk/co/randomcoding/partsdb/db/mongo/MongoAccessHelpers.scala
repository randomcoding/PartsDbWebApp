/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import com.mongodb.casbah.Imports._
import MongoConverters._
import net.liftweb.common.Logger

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
  def idNotInDb[T <: AnyRef](t: T, collection: MongoCollection)(implicit mf: Manifest[T]): Boolean = {
    val dbo: DBObject = t

    objectIdInDbQuery(dbo) match {
      case Some(query) => idNotInDb(collection, query)
      case _ => false
    }
  }

  /**
   * Checks the given collection contains an object with the [[uk.co.randomcoding.partsdb.core.id.Identifier]] of the provided object `(t)`.
   *
   * Can be used to check if an object has been added to the db by its id only
   *
   * @return `true` If the [[uk.co.randomcoding.partsdb.core.id.Identifier]] from `t` is present in the collection
   */
  def idIsInDb[T <: AnyRef](t: T, collection: MongoCollection)(implicit mf: Manifest[T]): Boolean = {
    val dbo: DBObject = t

    objectIdInDbQuery(dbo) match {
      case Some(query) => idIsInDb(collection, query)
      case _ => false
    }
  }

  /**
   * Gets the MongoDBObject that represents the item with the given id.
   *
   * @param item The
   */
  def getDbObject[T <: AnyRef](item: T, collection: MongoCollection)(implicit mf: Manifest[T]): MongoDBObject = {
    val dbItem: DBObject = item

    objectIdInDbQuery(dbItem) match {
      case Some(query) => {
        val objectWithIdentifier = collection.findOne(query).getOrElse(DBObject.empty)
        // This seems to be required to enable a correct implicit conversion to MongoDBObject
        objectWithIdentifier
      }
      case _ => MongoDBObject.empty
    }
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

  private def objectIdInDbQuery(dbo: MongoDBObject): Option[MongoDBObject] = {
    dbo.filterKeys(_.endsWith("Id")).toList match {
      case head :: tail => Some(head)
      case Nil => None
    }
  }

  private implicit def idEntryToMongoDBIdentifierQueryObject(idEntry: (String, AnyRef)): MongoDBObject = {
    val idQuery = MongoDBObject(idEntry._1 -> MongoDBObject("id" -> idEntry._2.asInstanceOf[BasicDBObject].as[Long]("id")))
    debug("Created idQuery: %s".format(idQuery))
    idQuery
  }
}