/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet.display

import uk.co.randomcoding.partsdb.lift.model.document.DocumentDataHolder

import net.liftweb.http.js.jquery.JqWiringSupport
import net.liftweb.http.WiringUI
import net.liftweb.util.Helpers._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait DocumentDataHolderTotalsDisplay {
  val dataHolder: DocumentDataHolder

  def renderDocumentTotals() = {
    "#subtotal *" #> WiringUI.asText(dataHolder.subTotal) &
      "#carriage *" #> WiringUI.asText(dataHolder.carriage) &
      "#vat *" #> WiringUI.asText(dataHolder.vatAmount) &
      "#total *" #> WiringUI.asText(dataHolder.totalCost, JqWiringSupport.fade)
  }
}