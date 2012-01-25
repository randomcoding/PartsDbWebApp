/**
 *
 */
package uk.co.randomcoding.partsdb.core.part

import net.liftweb.mongodb.record.field._
import net.liftweb.mongodb.record.{ MongoRecord, MongoMetaRecord }
import net.liftweb.record.field.DoubleField

/**
 * Provides a mapping for [[uk.co.randomcoding.partsdb.core.supplier.Supplier]]s to the
 * [[uk.co.randomcoding.partsdb.core.part.Part]]s they supply, the cost of the [[uk.co.randomcoding.partsdb.core.part.Part]]
 * and the last time it was purchased from them.
 *
 * @author Jane Rowe
 * @author RandomCoder - Changed to MongoRecord class
 */
class PartCost private () extends MongoRecord[PartCost] with ObjectIdPk[PartCost] {
  def meta = PartCost

  object part extends ObjectIdRefField(this, Part)
  object suppliedCost extends DoubleField(this)
  object lastSuppliedData extends DateField(this)
}

object PartCost extends PartCost with MongoMetaRecord[PartCost]