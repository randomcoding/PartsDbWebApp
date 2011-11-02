/**
 *
 */
package uk.co.randomcoding.partsdb.core.contact

import uk.co.randomcoding.partsdb.core.id.Identifier

/**
 * Some contact details.
 *
 * This provides a map of the name of a contact to the numbers/emails to use to contact them
 *
 * @constructor
 * @param id The [[uk.co.randomcoding.partsdb.core.id.Identifier]] for these details within the database
 * @param contacts A map of contact name -> contact number(s)
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
case class ContactDetails(val id: Identifier, val contacts: Map[String, Set[String]])