/**
 *
 */
package uk.co.randomcoding.partsdb.core.contact

import uk.co.randomcoding.partsdb.core.id.Identifier

/**
 * Some contact details for a customer or supplier.
 *
 * This provides a map of the name of a contact to the numbers/emails to use to contact them
 *
 * @constructor Create a new Contact details instance.
 *
 * @param contacts A map of contact name -> contact number(s)
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
case class ContactDetails(val contacts: Map[String, Set[String]])