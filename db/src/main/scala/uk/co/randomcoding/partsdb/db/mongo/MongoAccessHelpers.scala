/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import com.mongodb.casbah.Imports._

import MongoConverters._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object MongoAccessHelpers {

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
      case Some(query) => idNotInDb(query, collection)
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
      case Some(query) => idIsInDb(query, collection)
      case _ => false
    }
  }

  /*
   * Helper conversions and functions for the above functions
   */

  private val idNotInDb = (idQuery: MongoDBObject, collection: MongoCollection) => collection.findOne(idQuery).isEmpty

  private val idIsInDb = (idQuery: MongoDBObject, collection: MongoCollection) => collection.findOne(idQuery).isDefined

  private def objectIdInDbQuery(dbo: MongoDBObject): Option[MongoDBObject] = {
    dbo.filterKeys(_.endsWith("Id")).toList match {
      case head :: tail => Some(head)
      case Nil => None
    }
  }

  private implicit def idEntryToMongoDBIdentifierQueryObject(idEntry: (String, AnyRef)): MongoDBObject = {
    MongoDBObject(idEntry._1 -> MongoDBObject("id" -> idEntry._2.asInstanceOf[BasicDBObject].as[Long]("id")))
  }
}