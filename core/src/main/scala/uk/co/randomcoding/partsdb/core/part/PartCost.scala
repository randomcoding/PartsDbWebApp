/**
 *
 */
package uk.co.randomcoding.partsdb.core.part

import net.liftweb.mongodb.record.field._
import net.liftweb.mongodb.record.{ MongoRecord, MongoMetaRecord }
import net.liftweb.record.field._
import org.joda.time.DateTime
import org.bson.types.ObjectId
import com.foursquare.rogue.Rogue._
import java.util.Date
import net.liftweb.mongodb.record.BsonRecord
import net.liftweb.mongodb.record.BsonMetaRecord

/**
 * Provides a mapping for [[uk.co.randomcoding.partsdb.core.supplier.Supplier]]s to the
 * [[uk.co.randomcoding.partsdb.core.part.Part]]s they supply, the cost of the [[uk.co.randomcoding.partsdb.core.part.Part]]
 * and the last time it was purchased from them.
 *
 * @author Jane Rowe
 * @author RandomCoder - Changed to MongoRecord class
 */
class PartCost private () extends BsonRecord[PartCost] { // with ObjectIdPk[PartCost] {
  def meta = PartCost

  object part extends ObjectIdRefField(this, Part)
  object suppliedCost extends DoubleField(this)
  object lastSuppliedDate extends DateField(this)
  object supplierPartNumber extends StringField(this, 50)

  override def equals(that: Any): Boolean = {
    that match {
      case pc: PartCost => part.get == pc.part.get && suppliedCost.get == pc.suppliedCost.get && supplierPartNumber.get == pc.supplierPartNumber.get
      case _ => false
    }
  }

  override def hashCode: Int = getClass.hashCode + part.get.hashCode + suppliedCost.get.hashCode + supplierPartNumber.get.hashCode
}

object PartCost extends PartCost with BsonMetaRecord[PartCost] {

  /**
   * Creates a new `PartCost` record
   */
  def create(part: Part, cost: Double, lastSupplied: DateTime, supplierPartNumber: String): PartCost = {
    PartCost.createRecord.part(part.id.get).suppliedCost(cost).lastSuppliedDate(lastSupplied.toDate).supplierPartNumber(supplierPartNumber)
  }
}
