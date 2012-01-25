/**
 *
 */
package uk.co.randomcoding.partsdb.core.part

import net.liftweb.mongodb.record.{ MongoRecord, MongoMetaRecord }
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

/**
 * A collection of [[uk.co.randomcoding.partsdb.core.part.Part]]s sold together as a kit.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class PartKit private () extends MongoRecord[PartKit] with ObjectIdPk[PartKit] {
  def meta = PartKit

  object parts extends ObjectIdRefListField(this, Part)

  object cost extends DoubleField(this)
}

object PartKit extends PartKit with MongoMetaRecord[PartKit]