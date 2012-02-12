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
   * @param contactEmail Finds any customers that have this value in the  `emailAddress` field of their `contactDetails`
   */
  def findMatching(customerName: String = "", addressContains: String = "", contactName: String = "", contactPhoneNumber: String = "", contactMobileNumber: String = "", contactEmail: String = ""): Seq[Customer] = {
    val allCustomers = (Customer where (_.id exists true) fetch) toSet

    val matches = Map(customerName -> customerNameMatches(customerName),
      addressContains -> billingAddressMatches(addressContains),
      contactName -> contactNameMatches(contactName),
      contactPhoneNumber -> contactPhoneMatches(contactPhoneNumber),
      contactMobileNumber -> contactMobileMatches(contactMobileNumber),
      contactEmail -> contactEmailMatches(contactEmail))

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
    case name => {
      val contactIds = ContactDetails.where(_.contactName matches regexValue(name)).fetch map (_.id.get)

      Customer where (_.contactDetails in contactIds) fetch
    }
  }) toSet

  private val contactPhoneMatches = (phone: String) => (phone.trim match {
    case "" => Nil
    case p => {
      val contactIds = ContactDetails.where(_.phoneNumber matches regexValue(p)).fetch map (_.id.get)

      Customer where (_.contactDetails in contactIds) fetch
    }
  }) toSet

  private val contactMobileMatches = (mobile: String) => (mobile.trim match {
    case "" => Nil
    case p => {
      val contactIds = ContactDetails.where(_.mobileNumber matches regexValue(p)).fetch map (_.id.get)

      Customer where (_.contactDetails in contactIds) fetch
    }
  }) toSet

  private val contactEmailMatches = (email: String) => (email.trim match {
    case "" => Nil
    case e => {
      val contactIds = ContactDetails.where(_.emailAddress matches regexValue(e)).fetch map (_.id.get)

      Customer where (_.contactDetails in contactIds) fetch
    }
  }) toSet
}