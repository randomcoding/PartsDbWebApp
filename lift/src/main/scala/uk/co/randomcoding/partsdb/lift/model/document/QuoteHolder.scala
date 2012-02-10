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
import uk.co.randomcoding.partsdb.core.supplier.Supplier
import uk.co.randomcoding.partsdb.core.id.Identifier
import com.foursquare.rogue.Rogue._
import org.bson.types.ObjectId
import uk.co.randomcoding.partsdb.core.part.PartCost
import uk.co.randomcoding.partsdb.core.util.MongoFieldHelpers._

/**
 * Encapsulates the data required to generate a `Quote` document.
 *
 * To add/update line items set the current part (with [[uk.co.randomcoding.partsdb.lift.model.document.QuoteHolder#currentPart(Option[Part])]]
 * and then call [[uk.co.randomcoding.partsdb.lift.model.document.QuoteHolder#setPartQuantity(Int)]].
 *
 * To modify the markup used for the line item, call [[uk.co.randomcoding.partsdb.lift.model.document.QuoteHolder#markup(String)]] before
 * [[uk.co.randomcoding.partsdb.lift.model.document.QuoteHolder#updateCurrent(Int)]]
 *
 * This is used by the [[uk.co.randomcoding.partsdb.lift.snippet.AddQuote]] class as a cell.
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class QuoteHolder extends Logger {
  /**
   * The default markup rate for new lines
   */
  val DEFAULT_MARKUP = 25;

  // Cells to maintain values for current new line values

  /**
   * The currently selected part for the line
   */
  private val currentPartCell = ValueCell[Option[Part]](None)

  /**
   * Calculated value of the suppliers of a part
   */
  private val suppliersForPart = currentPartCell.lift(_ match {
    case Some(part) => {
      debug("Current Part is: %s. Generating list of suppliers who supply it")
      (None, "--Select Supplier--") :: (suppliedBy(part.id.get) map (supplier => (Some(supplier), supplier.supplierName.get)))
    }
    case _ => {
      debug("Current Part is not defined. Generating an empty list of suppliers")
      List((None, "--Select Part--"))
    }
  })

  private val suppliersCostsForPart = currentPartCell.lift(_ match {
    case Some(part) => {
      debug("Current Part is: %s. Generating list of suppliers who supply it")

      val suppliersToCostForPart = suppliedBy(part.id.get) map (supplier => (Some(supplier), supplier.suppliedParts.get.find(_.part.get == part.id.get)))
      suppliersToCostForPart filter (entry => (entry._1.isDefined && entry._2.isDefined))
    }
    case _ => {
      debug("Current Part is not defined. Generating an empty list of suppliers")
      List((None, None))
    }
  })

  private def suppliedBy(partId: ObjectId): List[Supplier] = Supplier where (_.suppliedParts.subfield(_.part) eqs partId) fetch

  /**
   * The current supplier of the part
   */
  private val currentSupplierCell = suppliersCostsForPart.lift(suppliersOfPart => {
    suppliersOfPart.map(entry => (entry._1.get, entry._2.get.suppliedCost.get)).sortBy(_._2).head
  })

  //ValueCell[Option[Supplier]](None)

  /**
   * Text representation of the current supplier's name
   */
  val supplierText = currentSupplierCell.lift(_ match {
    case (s: Supplier, price: Double) => s.supplierName.get
    case _ => "No Supplier Set"
  })

  /**
   * Calculated value of the base cost of the currently selected part
   */
  // This version uses the supplier
  /*private val currentPartBaseCostCell = currentPartCell.lift(currentSupplierCell)((_, _) match {
    case (Some(part), Some(supplier)) => PartCost where (_.id in supplier.suppliedParts) and (_.part eqs part.id.get) get match {
      case Some(pc) => pc.suppliedCost.get
      case _ => {
        error("No Suppliers found for part: %s".format(part))
        0.0d
      }
    }
    case (p, s) => {
      error("Expected a pair(Some(part), Some(Supplier)), but got (%s, %s)".format(p, s))
      0.0d
    }
  })*/
  /**
   * Calculated value of the base cost of the currently selected part
   *
   * No use of supplier
   *
   * This will return the cheapest price and set the supplier value
   */
  private val currentPartBaseCostCell = currentSupplierCell.lift(_ match {
    case (s: Supplier, price: Double) => price
    case _ => 0.0d
  })

  /*currentPartCell.lift(_ match {
    case Some(part) => {
      val cForPart = suppliersCostsForPart.get.filter(entry => (entry._1.isDefined && entry._2.isDefined)) map (entry => ((entry._1.get, entry._2.get, entry._2.get.suppliedCost.get)))
      //val sorted = cForPart.sortBy(entry => entry.3.)
      //val costForPart = cForPart.sortBy(tuple => tuple.2.get.suppliedCost.get.toDouble)
      PartCost where (_.part eqs part.id.get) orderAsc (_.suppliedCost) get match {
        case Some(pc) => {
          val cost = pc.suppliedCost.get

          cost
        }
        case _ => {
          //error("No Suppliers found for part: %s".format(part))
          error("Did not get a part cost for part %s".format(part))
          0.0d
        }
      }
    }
    case _ => {
      //error("Expected a pair(Some(part), Some(Supplier)), but got (%s, %s)".format(p, s))
      debug("Current part not set")
      0.0d
    }
  })*/

  private val quantityCell = ValueCell[Int](0)

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

  // TODO: These line item & totals cells can be moved into a common trait, as we will want to use the same functionality for other documents 
  /**
   * Holder for the current line items
   */
  private val lineItemsCell = ValueCell[List[LineItem]](Nil)

  /**
   * The total computed base cost of the line items, before tax
   */
  private val preTaxTotal = lineItemsCell.lift(_.foldLeft(0.0d)(_ + _.lineCost))

  /**
   * The tax rate. Set to 0.2 (20%)
   */
  val taxRate = ValueCell(0.2d)

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
   * Display the pre-tax total in £0.00 format.
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
   * Add a new [[uk.co.randomcoding.partsdb.core.document.LineItem]] to the quote.
   *
   * This gets the part from [[uk.co.randomcoding.partsdb.lift.model.document.QuoteHolder#currentPart()]], the markup from [[uk.co.randomcoding.partsdb.lift.model.document.QuoteHolder#markupCell]]
   * and the part cost from [[uk.co.randomcoding.partsdb.lift.model.document.QuoteHolder#currentPartCostCell]]
   *
   * If `0` is used for the quantity then the line item with the same part id as the current part will be removed.
   *
   * If a line item has already been added for the current part, then the quantity and markup values are updated to the currently set values.
   *
   * @param quant The quantity of the current part to use for the line item.
   */
  def addLineItem(): Unit = {
    (currentPart, quantityCell.get) match {
      case (None, _) => // do nothing
      case (Some(part), q) if q <= 0 => removeItem(part)
      case (Some(part), q) => {
        val partCost = currentPartBaseCostCell.get
        val markupValue = markupCell.get.toDouble / 100.0

        lineItemsCell.atomicUpdate(items => items.find(_.partId.get == part.id.get) match {
          case Some(lineItem) => items.map(li => {
            li.partId.get == part.id.get match {
              case true => updateLineItem(li, q, partCost, markupValue)
              case false => li
            }
          })
          case _ => items :+ LineItem.create(items.size, part, q, partCost, markupValue)
        })
      }
    }
  }

  private val updateLineItem = (li: LineItem, quant: Int, cost: Double, markupValue: Double) => li.quantity(quant).basePrice(cost).markup(markupValue)

  private def zero = BigDecimal(0)

  private def removeItem(part: Part) = {
    lineItemsCell.atomicUpdate(_.filterNot(_.partId.get == part.id.get))
    renumberLines
  }

  private def renumberLines = lineItemsCell.atomicUpdate(items => {
    var index = 0
    items sortBy (_.lineNumber.get) map (item => {
      val newItem = item.lineNumber(index)
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
   * @return The current markup value as a string.
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
      case Full(value) => value
      case _ => DEFAULT_MARKUP
    })

    debug("Manual cost is now: %d".format(markupCell.get.toInt))
  }

  def quantity = "%d".format(quantityCell.get)

  def quantity(q: Int) = quantityCell.set(q)

  /*def suppliers = {
    val s = suppliersForPart.get
    debug("Generates %s for 'suppliers'".format(s.mkString(", ")))
    s
  }*/

  /*def supplier(supplier: Option[Supplier]) = currentSupplierCell.set(supplier)

  def supplier = {
    val s = currentSupplierCell.get
    debug("Supplier is %s".format(s))
    s
  }*/

  /**
   * Gets the current line items, sorted by line number
   */
  def lineItems = lineItemsCell.get.sortBy(_.lineNumber.get)
}