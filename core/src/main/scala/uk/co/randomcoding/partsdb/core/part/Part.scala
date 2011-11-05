/**
 *
 */
package uk.co.randomcoding.partsdb.core.part

import uk.co.randomcoding.partsdb.core.id.Identifier

/**
 * @constructor Create a new part object
 * @param partId The [[uk.co.randomcoding.partsdb.core.id.Indentifier]] of this part. This is used for internal referencing of part objects from other entities.
 * @param partName The short (friendly) name of this part
 *
 * @author JMRowe <>
 *
 */
case class Part(val partId: Identifier, val partName: String)