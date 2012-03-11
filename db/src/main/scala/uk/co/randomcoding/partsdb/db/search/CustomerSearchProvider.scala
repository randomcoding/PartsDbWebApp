/**
 *
 */
package uk.co.randomcoding.partsdb.db.search

import com.foursquare.rogue.Rogue._
import uk.co.randomcoding.partsdb.core.customer.Customer
import scala.collection.mutable.{ Seq => MSeq }
import uk.co.randomcoding.partsdb.db.search.SearchHelpers._
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.contact.ContactDetails

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object CustomerSearchProvider {

  /**
   * Find the customers that match '''all''' the requirements
   *
   * All parameters default to an empty string.
   *
   * An empty string for a parameter indicates that no results should be returned for that item
   *
   * Each search parameter is used as a case insensitive '''regex''' sub string match, i.e. the search is treated as
   * `<value> matches ".*substring.*"`. Furthermore, the `substring` is `trim`ed before being used.
   *
   * @param customerName Value to find within the `customerName` field
   * @param addressContains Finds any customers that have a address that contains this value within the `addressText` field of their `businessAddress`
   * @param contactName Finds any customers that have this value in the  `customerName` field of their `contactDetails`
   * @param contactPhoneNumber Finds any customers that have this value in the  `phoneNumber` field of their `contactDetails`
   * @param contactMobileNumber Finds any customers that have this value in the  `mobileNumber` field of their `contactDetails`
   * @param contactFaxNumber Finds any customers that have this value in the  `faxNumber` field of their `contactDetails`
   * @param contactEmail Finds any customers that have this value in the  `emailAddress` field of their `contactDetails`
   */
  def findMatching(customerName: String = "", addressContains: String = "", contactName: String = "", contactPhoneNumber: String = "", contactMobileNumber: String = "", contactFax: String = "", contactEmail: String = ""): Seq[Customer] = {
    val allCustomers = (Customer where (_.id exists true) fetch) toSet

    val allEmpty = Seq(customerName, addressContains, contactName, contactPhoneNumber, contactMobileNumber, contactEmail, contactFax) filter (_ nonEmpty) isEmpty

    val matches = if (allEmpty) Map("All" -> allCustomers) else {
      Map(customerName -> customerNameMatches(customerName),
        addressContains -> billingAddressMatches(addressContains),
        contactName -> contactNameMatches(contactName),
        contactPhoneNumber -> contactPhoneMatches(contactPhoneNumber),
        contactMobileNumber -> contactMobileMatches(contactMobileNumber),
        contactEmail -> contactEmailMatches(contactEmail),
        contactFax -> contactFaxMatches(contactFax))
    }

    matches.filter(_._1 nonEmpty) map (_._2) match {
      case Nil => Seq.empty
      case matches => matches.foldLeft(allCustomers)(_ intersect _) toSeq
    }
  }

  private val customerNameMatches = (customerName: String) => (customerName.trim match {
    case "" => Nil
    case name => Customer where (_.customerName matches regexValue(name)) fetch
  }) toSet

  private val billingAddressMatches = (addressContains: String) => (addressContains.trim match {
    case "" => Nil
    case addr => {
      val addressIds = Address.where(_.addressText matches regexValue(addr)).fetch map (_.id.get)

      Customer.where(_.businessAddress in (addressIds)) fetch
    }
  }) toSet

  private val contactNameMatches = (contactName: String) => (contactName.trim match {
    case "" => Nil
    case name => Customer where (_.contactDetails.subfield(_.contactName) matches regexValue(name)) fetch
  }) toSet

  private val contactPhoneMatches = (phone: String) => (phone.trim match {
    case "" => Nil
    case p => Customer where (_.contactDetails.subfield(_.phoneNumber) matches regexValue(p)) fetch
  }) toSet

  private val contactMobileMatches = (mobile: String) => (mobile.trim match {
    case "" => Nil
    case p => Customer where (_.contactDetails subfield (_.mobileNumber) matches regexValue(p)) fetch
  }) toSet

  private val contactEmailMatches = (email: String) => (email.trim match {
    case "" => Nil
    case e => Customer where (_.contactDetails subfield (_.emailAddress) matches regexValue(e)) fetch
  }) toSet

  private val contactFaxMatches = (fax: String) => (fax.trim match {
    case "" => Nil
    case e => Customer where (_.contactDetails subfield (_.faxNumber) matches regexValue(e)) fetch
  }) toSet
}