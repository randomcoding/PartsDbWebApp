/**
 *
 */
package uk.co.randomcoding.partsdb.lift.model.document

import uk.co.randomcoding.partsdb.core.document.{ DocumentType, Document }
import net.liftweb.util.Cell
import uk.co.randomcoding.partsdb.core.customer.Customer

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class QuoteDocumentDataHolder(customer: Option[Customer]) extends DocumentDataHolder with NewLineItemDataHolder {
  customerCell.set(customer)

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
  }
}