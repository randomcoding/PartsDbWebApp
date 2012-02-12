/**
 *
 */
package uk.co.randomcoding.partsdb.core.contact

/**
 * Defines types of contact, EMail, Phone and Mobile
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
@deprecated("No longer in use since change to new MongoDB api", "0.1")
sealed abstract class ContactType

@deprecated("No longer in use since change to new MongoDB api", "0.1")
case class Email(emailAddress: String) extends ContactType

@deprecated("No longer in use since change to new MongoDB api", "0.1")
case class Mobile(mobileNumber: String, international: Boolean = false) extends ContactType

@deprecated("No longer in use since change to new MongoDB api", "0.1")
case class Phone(phoneNumber: String, international: Boolean = false) extends ContactType
