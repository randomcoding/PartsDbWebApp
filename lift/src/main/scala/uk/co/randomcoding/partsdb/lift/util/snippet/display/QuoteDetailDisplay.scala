/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet.display

import uk.co.randomcoding.partsdb.core.document.Document
import scala.xml.NodeSeq
import net.liftweb.util._
import net.liftweb.util.Helpers._
import org.joda.time.DateTime
import uk.co.randomcoding.partsdb.lift.util.LineItemDisplay
import uk.co.randomcoding.partsdb.core.document.LineItem
import scala.xml.Text

/**
 * Displays a series of quotes using the template `_quote_detail_display.html`
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object QuoteDetailDisplay {
  private val vatRate = 0.2d;

  def apply(quotes: List[Document]): Seq[CssSel] = {
    quotes map (quote => {
      val lineItems = quote.lineItems.get
      "#quoteId" #> quote.documentNumber &
        "#quotedOn" #> new DateTime(quote.createdOn.get).toString("dd/MM/yyyy") &
        "#lineItems" #> LineItemDisplay(lineItems) &
        "#subtotal" #> currencyFormat(subTotal(lineItems)) &
        "#carriage" #> Text(currencyFormat(quote.carriage.get)) &
        "#vat" #> currencyFormat(vatAmount(lineItems)) &
        "#total" #> currencyFormat(subTotal(lineItems) + vatAmount(lineItems) + quote.carriage.get)
    })
  }

  private def currencyFormat(value: Double): String = "£%02.2f".format(value)

  private def subTotal(lineItems: Seq[LineItem]) = lineItems map (_.lineCost) sum

  private def vatAmount(lineItems: Seq[LineItem]) = subTotal(lineItems) * vatRate
}