/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import com.mongodb.casbah.Imports._
import net.liftweb.json._
import net.liftweb.json.Serialization._
import uk.co.randomcoding.partsdb.core.address.{ Address, AddressId }

/**
 * Provides implicit conversions between `MongoDBObject`s and library objects
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
object MongoConverters {

  private implicit val formats = DefaultFormats

  /**
   * Converts from a [[com.mongodb.casbah.Imports.DBObject]] to a class defined within the application codebase.
   *
   * This is usually used to convert from the results returned from a query into types objects.
   *
   * @param mongoObject The object from the MongoDB database to convert
   * @tparam T The type to convert the object into
   * @return The converted object. This will be of type '''`T`'''
   */
  implicit def convertFromMongoDbObject[T](mongoObject: DBObject)(implicit mf: Manifest[T]): T = {
    val mObj = mongoObject.removeField("_id")
    val jsonString: String = mongoObject.toString
    read(jsonString)
  }

  /**
   * Converts a library object to a [[com.mongodb.casbah.Imports.DBObject]].
   *
   * This is used in the process of storing items in the database
   *
   * @param t The object to convert to a [[com.mongodb.casbah.Imports.DBObject]]. I think this '''must''' be a '''`case class`''' for this conversion to work
   * @return The [[com.mongodb.casbah.Imports.DBObject]]
   */
  implicit def convertToMongoDbObject[T <: AnyRef](t: T): DBObject = {
    com.mongodb.util.JSON.parse(t).asInstanceOf[DBObject]
  }

  private implicit def toJsonString[T <: AnyRef](o: T): String = {
    write(o)
  }
}