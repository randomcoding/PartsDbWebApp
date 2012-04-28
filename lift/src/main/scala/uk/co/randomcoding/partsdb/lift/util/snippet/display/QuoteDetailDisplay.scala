/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet.display

import org.joda.time.DateTime

import uk.co.randomcoding.partsdb.core.document.Document
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet._
import uk.co.randomcoding.partsdb.lift.util._

import net.liftweb.util.Helpers._
import net.liftweb.util.CssSel

/**
 * Displays a series of quotes using the template `_quote_detail_display.html`
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object QuoteDetailDisplay extends DocumentTotalsDisplay with PrintDocumentSnippet {

  def apply(quotes: List[Document], transactionId: String): Seq[CssSel] = {
    quotes map (quote => {
      "#quoteId" #> quote.documentNumber &
        "#quotedOn" #> new DateTime(quote.createdOn.get).toString("dd/MM/yyyy") &
        "#lineItems" #> LineItemDisplay(quote.lineItems.get) &
        renderDocumentTotals(quote) &
        "#raiseOrder" #> buttonLink("/app/order?transactionId=%s".format(transactionId), "Raise Order") &
        renderPrintDocument(quote)
    })
  }
}