/**
 *
 */
package uk.co.randomcoding.partsdb.lift.model.document

import scala.math.BigDecimal.double2bigDecimal
import uk.co.randomcoding.partsdb.core.document.{ LineItem, DocumentType, Document }
import uk.co.randomcoding.partsdb.core.id.DefaultIdentifier
import uk.co.randomcoding.partsdb.core.part.Part
import net.liftweb.util.ValueCell
import net.liftweb.util.Helpers._
import net.liftweb.common.Full
import net.liftweb.common.Logger

/**
 * Encapsulates the data required to generate a `Quote` document.
 *
 * This is used by the [[uk.co.randomcoding.partsdb.lift.snippet.AddQuote]] class as a cell.
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class QuoteHolder extends Logger {
  /**
   * The default markup rate for new lines
   */
  val DEFAULT_MARKUP = 25;

  /**
   * Holder for the current line items
   */
  val lineItems = ValueCell[List[LineItem]](Nil)

  // Cells to maintain values for current new line values

  /**
   * The currently selected part for the line
   */
  private val currentPartCell = ValueCell[Option[Part]](None)

  /**
   * Calculated value of the base cost of the currently selected part
   */
  private val currentPartBaseCostCell = currentPartCell.lift(_ match {
    case Some(part) => part.partCost
    case _ => 0.0d
  })

  /**
   * Holder for the current line's markup
   */
  private val markupCell = ValueCell[Int](DEFAULT_MARKUP)

  /**
   * Calculated value for the part cost of the current line.
   *
   * This applies the markup to the base cost
   */
  private val currentLinePartCost = currentPartBaseCostCell.lift(markupCell)((partBaseCost, markupPercentage) => partBaseCost + (partBaseCost * (markupPercentage / 100.0)))

  /**
   * The total computed base cost of the line items, before tax
   */
  private val preTaxTotal = lineItems.lift(_.foldLeft(0.0d)(_ + _.lineCost))

  /**
   * The tax rate. Set to 0.2 (20%)
   */
  private val taxRate = ValueCell(BigDecimal(0.2))

  /**
   * The computed value of the amount of tax for the quote
   */
  private val tax = preTaxTotal.lift(taxRate)(_ * _)

  /**
   * Calculated total cost of all line items
   */
  private val total = preTaxTotal.lift(tax)(_ + _)

  // Values for display in the GUI

  /**
   * Displays the base part cost in £0.00 format.
   *
   * Suitable for using as:
   * {{{
   * WiringUI.asText(holder.displayedBasePartCost
   * }}}
   */
  val displayedBasePartCost = currentPartCell.lift(_ match {
    case Some(part) => part.partCost
    case _ => 0.0d
  })

  /**
   * Display the pre tax total in £0.00 format.
   *
   * Suitable for use as:
   * {{{
   * WiringUI.asText(holder.subTotal)
   * }}}
   */
  val subTotal = preTaxTotal.lift("£%.2f".format(_))

  /**
   * The amount of vat for all the current line items
   *
   * Suitable for use as:
   * {{{
   * WiringUI.asText(holder.vatAmount)
   * }}}
   */
  val vatAmount = tax.lift("£%.2f".format(_))

  /**
   * The computed total of the line items plus tax in £0.00 format
   *
   * Suitable for use as:
   * {{{
   * WiringUI.asText(holder.totalCost)
   * }}}
   */
  val totalCost = total.lift("£%.2f".format(_))

  /**
   * Add a new [[uk.co.randomcoding.partsdb.core.document.LineItem]] to the quote
   */
  def setPartQuantity(part: Part, quant: Int): Unit = {
    if (quant <= 0) removeItem(part) else {
      val partCost = currentPartBaseCostCell.get
      val markupValue = markupCell.get.toDouble / 100.0
      debug("Using cost: %.2f".format(partCost))
      lineItems.atomicUpdate(items => items.find(_.partId == part.partId) match {
        case Some(lineItem) => items.map(li => li.copy(
          quantity = if (li.partId == part.partId) quant else li.quantity,
          basePrice = if (li.partId == part.partId) partCost else li.basePrice,
          markup = if (li.partId == part.partId) markupValue else li.markup))
        case _ => items :+ {
          val item = LineItem(items.size, part.partId, quant, partCost, markupValue)
          debug("Created: %s".format(item))
          item
        }
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

  // Accessors & Mutators for the state of the holder

  /**
   * Get the value of the current part from the holder
   *
   * @return an Option[Part] for the currently selected part, or `None` if no part has been selected
   */
  def currentPart = currentPartCell.get

  /**
   * Set the value of the current part in the holder
   */
  def currentPart(partOption: Option[Part]) = currentPartCell.set(partOption)

  /**
   * Get the display cell for the current part's base cost.
   *
   * If the current part is not set this will generate £0.00.
   *
   * Suitable for use as:
   * {{{
   * WiringUI.asText(holder.currentPartBaseCostDisplay)
   * }}}
   */
  def currentPartBaseCostDisplay = currentPartBaseCostCell.lift("£%.2f".format(_))

  /**
   * Get the value of the manual cost from the holder as a double with 2 decimal places precision
   *
   * @return A String in 0.00 format
   */
  def markup: String = "%d".format(markupCell.get.toInt)

  /**
   * Set the value of the markup percentage the holder from a String.
   *
   * If the string can be converted into an integer, that value is used, otherwise sets the value to `DEFAULT_MARKUP`
   */
  def markup(markupString: String) = {
    debug("Setting markup to: %s".format(markupString))
    markupCell.set(asInt(markupString) match {
      case Full(d) => d
      case _ => DEFAULT_MARKUP
    })

    debug("Manual cost is now: %d".format(markupCell.get.toInt))
  }
}