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

/**
 *
 */
package uk.co.randomcoding.partsdb.lift.model.document

import com.foursquare.rogue.Rogue._

import uk.co.randomcoding.partsdb.core.document.{ DocumentType, Document }
import net.liftweb.util.Cell
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.transaction.Transaction

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class QuoteDocumentDataHolder extends DocumentDataHolder with NewLineItemDataHolder {

  /**
   * The total computed base cost of the line items, before tax
   */
  override val preTaxTotal: Cell[Double] = itemsPreTaxSubTotal.lift(carriageCell)(_ + _)

  override val lineItemsSubTotalCell: Cell[Double] = itemsPreTaxSubTotal.lift(_ + 0.0d)

  /**
   * Populate this data holder with the data from a Document.
   *
   * The document '''must''' be a Quote otherwise an `IllegalArgumentException]] exception is thrown
   */
  @throws(classOf[IllegalArgumentException])
  def populate(quote: Document) {
    require(quote.documentType.get == DocumentType.Quote, "Document must be a Quote")
    carriage = quote.carriage.get
    quote.lineItems.get foreach (addLineItem)

    customer = Transaction.where(_.documents contains quote.id.get).get match {
      case Some(trans) => Customer.findById(trans.customer.get)
      case _ => None
    }
  }
}
