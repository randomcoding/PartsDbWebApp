/**
 *
 */
package uk.co.randomcoding.partsdb.lift.model.document

import scala.math.BigDecimal.double2bigDecimal

import uk.co.randomcoding.partsdb.core.document.{ LineItem, DocumentType, Document }
import uk.co.randomcoding.partsdb.core.id.DefaultIdentifier
import uk.co.randomcoding.partsdb.core.part.Part

import net.liftweb.util.ValueCell

/**
 * Encapsulates the data required to generate a `Quote` document.
 *
 * This is used by the [[uk.co.randomcoding.partsdb.lift.snippet.AddQuote]] class as a cell.
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class QuoteHolder {
  /**
   * `ValueCell` that maintains the current line items
   */
  val lineItems = ValueCell[List[LineItem]](Nil)

  /**
   * The total computed value of the items, before tax
   */
  val preTaxTotal = lineItems.lift(_.foldLeft(0.0d)(_ + _.lineCost))

  val subTotal = preTaxTotal.lift("£%.2f".format(_))

  /**
   * The tax rate. Defaults to 0.2 (20%)
   */
  val taxRate = ValueCell(BigDecimal(0.2))

  /**
   * The computed value of the amount of tax for the quote
   */
  val tax = preTaxTotal.lift(taxRate)(_ * _)

  val vatAmount = tax.lift("£%.2f".format(_))
  /**
   * The computed total of the line items plus tax.
   */
  val total = preTaxTotal.lift(tax)(_ + _)

  val totalCost = total.lift("£%.2f".format(_))

  /**
   * Add a new [[uk.co.randomcoding.partsdb.core.document.LineItem]] to the quote
   */
  def setPartQuantity(part: Part, quant: Int): Unit = {
    if (quant <= 0) removeItem(part) else {
      lineItems.atomicUpdate(items => items.find(_.partId == part.partId) match {
        case Some(lineItem) => items.map(li => li.copy(quantity = if (li.partId == part.partId) quant else li.quantity))
        case _ => items :+ LineItem(items.size, part.partId, quant, part.partCost)
      })
    }
  }

  /**
   * Build the Quote document represented by the
   */
  def buildQuote = Document(DefaultIdentifier, DocumentType.Quote, lineItems.get, DefaultIdentifier)

  /**
   * Gets the current line items, sorted by line number
   */
  def quoteItems = lineItems.get.sortBy(_.lineNumber)

  def zero = BigDecimal(0)

  private def removeItem(part: Part) = {
    lineItems.atomicUpdate(_.filterNot(_.partId == part.partId))
    renumberLines
  }

  private def renumberLines = lineItems.atomicUpdate(items => {
    var index = 0
    items sortBy (_.lineNumber) map (item => {
      val newItem = item.copy(lineNumber = index)
      index += 1
      newItem
    })
  })

}