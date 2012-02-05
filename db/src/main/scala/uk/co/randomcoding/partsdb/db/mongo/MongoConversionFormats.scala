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
trait MongoConversionFormats {
  implicit val formats = DefaultFormats
}