/**
 *
 */
package uk.co.randomcoding.partsdb.core.part

import net.liftweb.mongodb.record.field._
import net.liftweb.mongodb.record.{ MongoRecord, MongoMetaRecord }

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class PartKit extends MongoRecord[PartKit] with ObjectIdPk[PartKit] {
  def meta = PartKit

  object parts extends ObjectIdRefListField(this, Part)
}

object PartKit extends PartKit with MongoMetaRecord[PartKit]