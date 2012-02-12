/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import net.liftweb.json.DefaultFormats

/**
 * Provides a consistent formats object to use for implementation and testing
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
@deprecated("Now using different DB AccessAPI", "0.1")
trait MongoConversionFormats {
  implicit val formats = DefaultFormats
}