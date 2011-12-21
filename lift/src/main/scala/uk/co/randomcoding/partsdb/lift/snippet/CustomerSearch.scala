/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import net.liftweb.util.Helpers._
import net.liftweb.http.js.JsCmds._
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.db.search.MongoSearchTerm
import uk.co.randomcoding.partsdb.lift.util.CustomerDisplay
import uk.co.randomcoding.partsdb.lift.util.snippet.DbAccessSnippet
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.db.search.CustomerSearchProvider
import net.liftweb.http.js.JsCmds
import scala.util.matching.Regex

/**
 * Snippet to perform search for customers
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object CustomerSearch extends DbAccessSnippet {
  private val searchKeys = List("customerName", "billingAddress", "contactDetails.contactName", "contactDetails.phoneNumbers", "contactDetails.mobileNumbers", "contactDetails.emailAddresses")

  def render = {
    var customerName = ""
    var businessAddress = ""
    var contactName = ""
    var phoneNumber = ""
    var mobileNumber = ""
    var email = ""

    def regexTerm(key: String, subString: String) = MongoSearchTerm(key, ".*%s.*".format(subString.trim).r)
    // should generate search terms or leave up to factory method?
    def searchTerms = searchKeys map { key =>
      key match {
        case "customerName" => regexTerm(key, customerName)
        case "billingAddress" => regexTerm(key, businessAddress)
        case s if s endsWith "contactName" => regexTerm(key, contactName)
        case s if s endsWith "phoneNumbers" => regexTerm(key, phoneNumber)
        case s if s endsWith "mobileNumbers" => regexTerm(key, mobileNumber)
        case s if s endsWith "emailAddresses" => regexTerm(key, email)
      }
    } filter (_.searchValue != ".*.*")

    /**
     * Generate the new live search results and display then in the relevant section of the page
     *
     * This gets the search terms
     */
    def updateResults(s: String = "") = {
      val results = searchTerms match {
        case Nil => getAll[Customer]("customerId")
        case terms => CustomerSearchProvider(collection).find(searchTerms.toSet)
      }

      val resultsHtml = CustomerDisplay.displayTable(results)
      JsCmds.SetHtml("results", resultsHtml)
    }

    "#customerNameEntry" #> styledAjaxText(customerName, updateResults) &
      "#businessAddressEntry" #> styledAjaxText(businessAddress, updateResults) &
      "#contactNameEntry" #> styledAjaxText(contactName, updateResults) &
      "#phoneNumberEntry" #> styledAjaxText(phoneNumber, updateResults) &
      "#mobileNumberEntry" #> styledAjaxText(mobileNumber, updateResults) &
      "#emailEntry" #> styledAjaxText(email, updateResults)
  }
}