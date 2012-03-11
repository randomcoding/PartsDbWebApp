/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet.display

import uk.co.randomcoding.partsdb.lift.model.document.QuoteHolder

import net.liftweb.http.js.jquery.JqWiringSupport
import net.liftweb.http.WiringUI
import net.liftweb.util.Helpers._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait QuoteHolderDocumentTotalsDisplay {
  val quoteHolder: QuoteHolder

  def renderDocumentTotals() = {
    "#subtotal" #> WiringUI.asText(quoteHolder.subTotal) &
      "#carriage" #> WiringUI.asText(quoteHolder.carriage) &
      "#vat" #> WiringUI.asText(quoteHolder.vatAmount) &
      "#total" #> WiringUI.asText(quoteHolder.totalCost, JqWiringSupport.fade)
  }
}