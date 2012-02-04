/**
 *
 */
package uk.co.randomcoding.partsdb.core.supplier

import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.core.part.PartCost
import net.liftweb.mongodb.record.{ MongoRecord, MongoMetaRecord }
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

/**
 * Supplier information including contact details and a free form notes
 *
 * @author Jane Rowe
 * @author RandomCoder - Changed to MongoRecord class
 *
 */
class Supplier private () extends MongoRecord[Supplier] with ObjectIdPk[Supplier] {
  def meta = Supplier

  /**
   * The name of the supplier
   */
  object supplierName extends StringField(this, 50)

  /**
   * The contact details for this supplier
   */
  object contactDetails extends ObjectIdRefField(this, ContactDetails)

  /**
   * The parts this supplier provides as [[uk.co.randomcoding.partsdb.core.part.PartCost]] objects
   */
  object suppliedParts extends ObjectIdRefListField(this, PartCost)

  /**
   * Any notes about this supplier.
   *
   * These are optional
   */
  object notes extends OptionalStringField(this, 500)
}

object Supplier extends Supplier with MongoMetaRecord[Supplier] {

}
