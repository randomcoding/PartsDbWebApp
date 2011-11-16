/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

import net.liftweb.json.DefaultFormats
import uk.co.randomcoding.partsdb.db.mongo.MongoConversionHints._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait MongoConversionFormats {
  implicit val formats = DefaultFormats //.withHints(contactTypeHints)
}