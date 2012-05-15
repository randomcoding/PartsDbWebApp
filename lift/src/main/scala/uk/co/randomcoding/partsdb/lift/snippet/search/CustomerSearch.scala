/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet.search

import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.db.search.SearchHelpers._
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers.styledAjaxText
import uk.co.randomcoding.partsdb.lift.util.CustomerDisplay
import net.liftweb.util.Helpers._
import net.liftweb.http.SHtml._
import net.liftweb.http.js.{ JsCmds, JsCmd }
import net.liftweb.util.Helpers._
import com.foursquare.rogue.Rogue._
import com.foursquare.rogue.EqClause
import java.util.regex.Pattern
import com.foursquare.rogue.DocumentScan
import uk.co.randomcoding.partsdb.db.search.CustomerSearchProvider

/**
 * Snippet to perform search for customers
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object CustomerSearch {
  def render = {
    var customerName = ""
    var businessAddress = ""
    var contactName = ""
    var phoneNumber = ""
    var mobileNumber = ""
    var faxNumber = ""
    var email = ""

    /**
     * Generate the new live search results and display then in the relevant section of the page
     *
     * This gets the search terms, performs the search and displays the results in the `results` div of the main page
     */
    def updateResults(s: String = "") = {
      val results = CustomerSearchProvider.findMatching(customerName, businessAddress, contactName, phoneNumber, mobileNumber, faxNumber, email).toList

      JsCmds.SetHtml("results", displayResults(results))
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
      "#faxNumberEntry" #> styledAjaxText(faxNumber, (s: String) => updateValue(() => faxNumber = s)(s)) &
      "#emailEntry" #> styledAjaxText(email, (s: String) => updateValue(() => email = s)(s)) &
      "#results" #> displayResults(Customer fetch)
  }

  private def displayResults(results: List[Customer]) = CustomerDisplay(results.sortBy(_.customerName.get), displayLink = true)
}