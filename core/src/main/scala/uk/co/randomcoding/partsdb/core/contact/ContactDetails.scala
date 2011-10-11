/**
 *
 */
package uk.co.randomcoding.partsdb.core.contact

/**
 * Some contact details.
 *
 * @constructor
 * @param contacts A map of contact name -> contact number(s)
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
case class ContactDetails(contacts: Map[String, Set[String]])