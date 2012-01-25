/**
 *
 */
package uk.co.randomcoding.partsdb.core.address

import net.liftweb.mongodb.record.field._
import net.liftweb.mongodb.record.{ MongoRecord, MongoMetaRecord }
import net.liftweb.record.field.StringField

/**
 * A simple representation of an address.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
class Address private () extends MongoRecord[Address] with ObjectIdPk[Address] {
  def meta = Address

  object shortName extends StringField(this, 50)
  object addressText extends StringField(this, 300)
  object country extends StringField(this, 50)
}

object Address extends Address with MongoMetaRecord[Address]
