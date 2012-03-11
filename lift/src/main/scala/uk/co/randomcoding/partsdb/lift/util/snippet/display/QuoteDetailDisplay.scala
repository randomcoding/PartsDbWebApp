/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet.display

import scala.xml.Text

import org.joda.time.DateTime

import uk.co.randomcoding.partsdb.core.document.Document
import uk.co.randomcoding.partsdb.lift.util._

import net.liftweb.http.SHtml._
import net.liftweb.util.Helpers._
import net.liftweb.util.CssSel

/**
 * Displays a series of quotes using the template `_quote_detail_display.html`
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object QuoteDetailDisplay extends DocumentTotalsDisplay {

  def apply(quotes: List[Document]): Seq[CssSel] = {
    quotes map (quote => {
      val lineItems = quote.lineItems.get
      "#quoteId" #> quote.documentNumber &
        "#quotedOn" #> new DateTime(quote.createdOn.get).toString("dd/MM/yyyy") &
        "#lineItems" #> LineItemDisplay(lineItems) &
        renderDocumentTotals(quote) &
        "#raiseOrder" #> link("/app/order", () => (), Text("Raise Order"))
    })
  }
}