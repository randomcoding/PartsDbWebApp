/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import com.mongodb.casbah.Imports._
import net.liftweb.json._
import net.liftweb.json.Serialization._
import uk.co.randomcoding.partsdb.core.address.{ Address, AddressId }

/**
 * Provides implicit conversions between DBObjects and library objects
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
object MongoConverters {

  private implicit val formats = DefaultFormats

  implicit def convertFromMongoDbObject[T](mongoObject: DBObject): T = {
    val jsonString: String = mongoObject.toString
    fromJsonString(jsonString)
  }

  implicit def convertToMongoDbObject[T <: AnyRef](t: T): DBObject = {
    import com.mongodb.util.JSON.{ parse => jsonToMongoDB }
    jsonToMongoDB(t).asInstanceOf[DBObject]
  }

  private implicit def toJsonString[T <: AnyRef](o: T): String = {
    write(o)
  }

  private implicit def fromJsonString[T <: AnyRef](o: String): T = {
    read(o)
  }
}