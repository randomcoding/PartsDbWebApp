/*
 * Copyright (C) 2012 RandomCoder <randomcoder@randomcoding.co.uk>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *    RandomCoder - initial API and implementation and/or initial documentation
 */
package uk.co.randomcoding.partsdb.lift.util.snippet.display

import scala.xml.Text

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
        "#raiseOrder" #> (if (quote.editable.get) buttonLink("Raise Order", "/app/order?transactionId=%s".format(transactionId)) else Text("")) &
        renderPrintDocument(quote) &
        "#editQuoteButton" #> (if (quote.editable.get) buttonLink("Edit Quote", "/app/quote?id=%s".format(quote.id.get)) else Text(""))
    })
  }
}
