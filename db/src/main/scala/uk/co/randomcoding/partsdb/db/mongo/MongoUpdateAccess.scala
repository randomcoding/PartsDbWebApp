/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import com.mongodb.casbah.Imports._
import uk.co.randomcoding.partsdb.db.mongo.MongoConverters._

/**
 * Provides an implementation of the capability to add an object to a collection
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
trait MongoUpdateAccess {
  val collection: MongoCollection

  def add[T <: AnyRef](t: T): Unit = addIfNotAlreadyInDb(t)

  private def addIfNotAlreadyInDb(dbo: DBObject): Unit = {
    objectIdInDbQuery(dbo) match {
      case Some(idInDbQuery) => {
        if (!(alreadyInDb(idInDbQuery))) collection += dbo
      }
      case _ => // throw exception?
    }
  }

  private def alreadyInDb(idQuery: MongoDBObject) = collection.findOne(idQuery).isDefined

  private def objectIdInDbQuery(dbo: DBObject): Option[MongoDBObject] = {
    dbo.filterKeys(_.endsWith("Id")).toList match {
      case head :: tail => Some(head)
      case Nil => None
    }
  }

  private implicit def idEntryToMongoDBIdentifierQueryObject(idEntry: (String, AnyRef)): MongoDBObject =
    MongoDBObject(idEntry._1 -> MongoDBObject("id" -> idEntry._2.asInstanceOf[BasicDBObject].as[Long]("id")))
}