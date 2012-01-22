/**
 *
 */
package uk.co.randomcoding.partsdb.core.part

import uk.co.randomcoding.partsdb.core.id.{ Identifier, Identifiable }
import uk.co.randomcoding.partsdb.core.id.DefaultIdentifier
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import uk.co.randomcoding.partsdb.core.supplier.{ Supplier, DefaultSupplier }
import java.util.Date

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
 *
 */
case class Part(val partId: Identifier, val partName: String, val vehicles: Option[Vehicle] = None, val modId: Option[String] = None) extends Identifiable {
  override val identifierFieldName = "partId"
}

object DefaultPart extends Part(DefaultIdentifier, "No Part")

/**
 * @constructor Create a new PartKit object which is a collection of parts
 * @param partsId The [[uk.co.randomcoding.partsdb.core.id.Identifier]] of this part collection. This is used for internal referencing of part collection objects from other entities.
 * @param partsName The short (friendly) name of this part collection
 * @param cost The aggregated cost of this part collection
 *
 * @author Jane Rowe
 *
 */
case class PartKit(val kitId: Identifier, val kitName: String, val parts: List[Part]) extends Identifiable {
  override val identifierFieldName = "kitId"
}

