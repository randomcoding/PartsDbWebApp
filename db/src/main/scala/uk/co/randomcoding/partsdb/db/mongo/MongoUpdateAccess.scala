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

  def add[T <: AnyRef](t: T): Unit = {
    val dbo: DBObject = t
    addIfNotAlreadyInDb(dbo)
    /*getObjectId(dbo) match {
      case Some(idValue) => addIfNotAlreadyInDb(dbo)
      case _ =>
    }
    collection += dbo*/
  }

  private def addIfNotAlreadyInDb(dbo: DBObject): Unit = {
    getObjectId(dbo) match {
      case Some(idValue) => {
        val query = MongoDBObject("id" -> idValue)
        collection.findOne(query).toList match {
          case Nil => collection += dbo
          case _ => // TODO throw an exception
        }
      }
      case _ => // throw exception
    }
  }

  private def getObjectId(dbo: DBObject): Option[Long] = {
    val vals = dbo.values.toList
    val idVal = for {
      value <- dbo.values.toList
      if value.isInstanceOf[BasicDBObject]
    } yield {
      value.asInstanceOf[BasicDBObject].as[Long]("id")
    }

    idVal match {
      case Nil => None
      case head :: tail if head.isInstanceOf[Long] => Some(head)
      case _ => None
    }
  }
}