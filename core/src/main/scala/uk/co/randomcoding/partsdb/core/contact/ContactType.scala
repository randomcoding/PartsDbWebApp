/**
 *
 */
package uk.co.randomcoding.partsdb.core.contact

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */

//package contacttype {
sealed abstract class ContactType

case class Email(emailAddress: String) extends ContactType

case class Mobile(mobileNumber: String, international: Boolean = false) extends ContactType

case class Phone(phoneNumber: String, international: Boolean = false) extends ContactType

//}