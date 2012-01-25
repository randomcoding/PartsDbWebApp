/**
 *
 */
package uk.co.randomcoding.partsdb.core.part

import net.liftweb.mongodb.record.field._
import net.liftweb.mongodb.record.{ MongoRecord, MongoMetaRecord }
import net.liftweb.record.field.DoubleField

/**
 * @constructor Create a new PartCost object
 * @param partCostid The [[uk.co.randomcoding.partsdb.core.id.Identifier]] of this part cost. This is used for internal referencing of part collection objects from other entities.
 * @param part The [[uk.co.randomcoding.partsdb.core.part.Part]] of this part cost.
 * @param supplier The [[uk.co.randomcoding.partsdb.core.supplier.Supplier]] of this part cost.
 * @param quoteDate The date the quote for this part was made.
 *
 * @author Jane Rowe
 * @author RandomCoder - Changed to MongoRecord class
 */
class PartCost extends MongoRecord[PartCost] with ObjectIdPk[PartCost] {
  def meta = PartCost

  object part extends ObjectIdRefField(this, Part)
  object suppliedCost extends DoubleField(this)
  object lastSuppliedData extends DateField(this)
}

object PartCost extends PartCost with MongoMetaRecord[PartCost]
/*case class PartCost(val partCostId: Identifier, val part: Part, val suppliedCost: Double, val lastSuppliedDate: Date) extends Identifiable {
  override val identifierFieldName = "partCostId"
}

object DefaultPartCost extends PartCost(DefaultIdentifier, DefaultPart, 0.00, java.util.Calendar.getInstance().getTime())*/
