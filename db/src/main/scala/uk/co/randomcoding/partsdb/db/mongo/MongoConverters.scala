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

  implicit def convertFromMongoDbObject[T](mongoObject: DBObject)(implicit mf: Manifest[T]): T = {
    val mObj = mongoObject.removeField("_id")
    val jsonString: String = mongoObject.toString
    read(jsonString)
  }

  implicit def convertToMongoDbObject[T <: AnyRef](t: T): DBObject = {
    com.mongodb.util.JSON.parse(t).asInstanceOf[DBObject]
  }

  private implicit def toJsonString[T <: AnyRef](o: T): String = {
    write(o)
  }
}