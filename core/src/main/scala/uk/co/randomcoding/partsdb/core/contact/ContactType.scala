/**
 *
 */
package uk.co.randomcoding.partsdb.core.contact

/**
 * Defines types of contact, EMail, Phone and Mobile
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */

sealed abstract class ContactType

case class Email(emailAddress: String) extends ContactType

case class Mobile(mobileNumber: String, international: Boolean = false) extends ContactType

case class Phone(phoneNumber: String, international: Boolean = false) extends ContactType
