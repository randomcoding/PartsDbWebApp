/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet.search

import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.db.search.SearchKeys._
import uk.co.randomcoding.partsdb.db.search.{ SearchKeys, MongoSearchTerm }
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers.styledAjaxText
import uk.co.randomcoding.partsdb.lift.util.CustomerDisplay

import net.liftweb.util.Helpers._
import net.liftweb.http.SHtml._
import net.liftweb.http.js.{ JsCmds, JsCmd }
import net.liftweb.util.Helpers._

/**
 * Snippet to perform search for customers
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object CustomerSearch {
  private val searchKeys = List(customerName, billingAddressText, contactsName, contactsPhoneNumber, contactsMobileNumber, contactsEmailAddress)

  def render = {
    var customerName = ""
    var businessAddress = ""
    var contactName = ""
    var phoneNumber = ""
    var mobileNumber = ""
    var email = ""

    /**
     * Helper function to generate a regex search term
     */
    def regexTerm(key: String, subString: String) = MongoSearchTerm(key, ".*%s.*".format(subString.trim).r)

    /**
     * Generates a list of unique, non empty, [[uk.co.randomcoding.partsdb.db.search.MongoSearchTerm]]s
     * used to find the matching enreies in the database,
     */
    def searchTerms = searchKeys map { key =>
      key match {
        case SearchKeys.customerName => regexTerm(key, customerName)
        case SearchKeys.billingAddressText => regexTerm(key, businessAddress)
        case SearchKeys.contactsName => regexTerm(key, contactName)
        case SearchKeys.contactsPhoneNumber => regexTerm(key, phoneNumber)
        case SearchKeys.contactsMobileNumber => regexTerm(key, mobileNumber)
        case SearchKeys.contactsEmailAddress => regexTerm(key, email)
      }
    } filterNot (_.searchValue.toString == ".*.*")

    /**
     * Generate the new live search results and display then in the relevant section of the page
     *
     * This gets the search terms, performs the search and displays the results in the `results` div of the main page
     */
    def updateResults(s: String = "") = {
      val results = searchTerms match {
        /*case Nil => getAll[Customer]("customerId")
        case terms => CustomerSearchProvider(collection).find(searchTerms.toSet)*/
        case _ => List.empty[Customer]
      }

      JsCmds.SetHtml("results", CustomerDisplay.displayTable(results.sortBy(_.customerName.get)))
    }

    /**
     * Convenience function to update the value of a variable and then return a partial function of `updateResults(String)`
     */
    def updateValue(func: () => Any): (String) => JsCmd = {
      func()
      updateResults(_: String)
    }

    "#customerNameEntry" #> styledAjaxText(customerName, (s: String) => updateValue(() => customerName = s)(s)) &
      "#businessAddressEntry" #> styledAjaxText(businessAddress, (s: String) => updateValue(() => businessAddress = s)(s)) &
      "#contactNameEntry" #> styledAjaxText(contactName, (s: String) => updateValue(() => contactName = s)(s)) &
      "#phoneNumberEntry" #> styledAjaxText(phoneNumber, (s: String) => updateValue(() => phoneNumber = s)(s)) &
      "#mobileNumberEntry" #> styledAjaxText(mobileNumber, (s: String) => updateValue(() => mobileNumber = s)(s)) &
      "#emailEntry" #> styledAjaxText(email, (s: String) => updateValue(() => email = s)(s)) /*&
      "#results" #> CustomerDisplay.displayTable(getAll[Customer]("customerId"))*/
  }
}