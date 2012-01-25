/**
 *
 */
package uk.co.randomcoding.partsdb.core.part

import uk.co.randomcoding.partsdb.core.id.{ Identifier, Identifiable }
import uk.co.randomcoding.partsdb.core.id.DefaultIdentifier
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import uk.co.randomcoding.partsdb.core.supplier.{ Supplier, DefaultSupplier }
import java.util.Date
import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.mongodb.record.field.ObjectIdRefField
import net.liftweb.record.field.OptionalStringField
import net.liftweb.record.field.StringField

/**
 * @constructor Create a new part object
 * @param partId The [[uk.co.randomcoding.partsdb.core.id.Identifier]] of this part. This is used for internal referencing of part objects from other entities.
 * @param partName The short (friendly) name of this part
 * @param vehicles A List of [[uk.co.randomcoding.partsdb.core.vehicle.Vehicle]] that can use this part
 * @param supplied A List of a tuple of [[uk.co.randomcoding.partsdb.core.supplier.Supplier, cost]] that covers the supplier with the costs
 *
 * @param partIdMod The MoD identification value for this part
 *
 * val vehicles: Option[List[Vehicle]] = None
 *
 * @author Jane Rowe
 * @author RandomCoder - Changed to MongoRecord class
 *
 */
class Part extends MongoRecord[Part] with ObjectIdPk[Part] {
  def meta = Part

  object partName extends StringField(this, 50)
  object vehicle extends ObjectIdRefField(this, Vehicle)
  object modIf extends OptionalStringField(this, 50)
}

object Part extends Part with MongoMetaRecord[Part]

/*case class Part(val partId: Identifier, val partName: String, val vehicles: Option[Vehicle] = None, val modId: Option[String] = None) extends Identifiable {
  override val identifierFieldName = "partId"
}

object DefaultPart extends Part(DefaultIdentifier, "No Part")*/

