/**
 *
 */
package uk.co.randomcoding.partsdb.core.util

import org.bson.types.ObjectId

/**
 * Helper functions and implicit conversions to make Mongo DB a little easier to use with the PDWA API
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object MongoHelpers {

  /**
   * Convert a String to a BSON `ObjectId`. If the String is not a valid `ObjectId` this implicit conversion will fail at runtime
   */
  implicit def stringToObjectId(oid: String): ObjectId = new ObjectId(oid)
}