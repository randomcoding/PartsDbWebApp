package uk.co.randomcoding.partsdb.core.part

import uk.co.randomcoding.partsdb.core.id.{ Identifier, Identifiable, DefaultIdentifier }

/**
 * The data object for a PartKit.
 *
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

object DefaultPartKit extends PartKit(DefaultIdentifier, "Default PartKit", List[Part]())