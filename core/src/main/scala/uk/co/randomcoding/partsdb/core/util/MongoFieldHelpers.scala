/**
 *
 */
package uk.co.randomcoding.partsdb.core.util

import net.liftweb.record.field._
import net.liftweb.mongodb.record.field.ObjectIdRefField
import org.bson.types.ObjectId
import net.liftweb.mongodb.record.field.ObjectIdRefListField

/**
 * Contains implicit conversions for basic field types to their values. This avoids the continual use of `.get`
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object MongoFieldHelpers {

  implicit def doubleFieldToDouble(field: DoubleField[_]): Double = field.get

  implicit def intFieldToDouble(field: IntField[_]): Int = field.get

  implicit def stringFieldToString(field: StringField[_]): String = field.get

  implicit def typedIdFieldToId(field: ObjectIdRefField[_, _]): ObjectId = field.get

  implicit def typedListIdFieldToId(field: ObjectIdRefListField[_, _]): List[ObjectId] = field.get
}