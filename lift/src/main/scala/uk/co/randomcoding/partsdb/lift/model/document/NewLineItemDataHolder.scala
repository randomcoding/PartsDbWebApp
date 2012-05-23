/**
 *
 */
package uk.co.randomcoding.partsdb.lift.model.document

import uk.co.randomcoding.partsdb.core.supplier.Supplier
import org.bson.types.ObjectId
import net.liftweb.util.ValueCell
import uk.co.randomcoding.partsdb.core.part.Part
import com.foursquare.rogue.Rogue._
import net.liftweb.common.Logger
import net.liftweb.util.Helpers._
import net.liftweb.common.Full
import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.field.ObjectIdPk
import uk.co.randomcoding.partsdb.core.part.PartKit

/**
 * A data container for adding new line items via a page.
 *
 * To add/update line items set the current part (with [[uk.co.randomcoding.partsdb.lift.model.document.DocumentDataHolder#currentPart(Option[Part])]]
 * and then call [[uk.co.randomcoding.partsdb.lift.model.document.DocumentDataHolder#setPartQuantity(Int)]].
 *
 * To modify the markup used for the line item, call [[uk.co.randomcoding.partsdb.lift.model.document.DocumentDataHolder#markup(String)]] before
 * [[uk.co.randomcoding.partsdb.lift.model.document.DocumentDataHolder#updateCurrent(Int)]]
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait NewLineItemDataHolder extends LineItemsDataHolder with Logger {
  /**
   * The default markup rate for new lines
   */
  val DEFAULT_MARKUP = 25;

  // Cells to maintain values for current new line values

  /**
   * The currently selected part for the line
   */
  private val currentPartCell = ValueCell[Option[MongoRecord[_] with ObjectIdPk[_]]](None)

  /**
   * Calculated value of the suppliers of a part
   */
  private val suppliersForPart = currentPartCell.lift(_ match {
    case Some(item) => {
      item match {
        case part: Part => {
          debug("Current Part is: %s. Generating list of suppliers who supply it")
          (None, "Select Supplier") :: (suppliedBy(part.id.get) map (supplier => (Some(supplier), supplier.supplierName.get)))
        }
        case partKit: PartKit => List((None, "Select Supplier"), (Supplier where (_.supplierName eqs "C.A.T.9 Limited") get, "C.A.T.9 Limited"))
        case other => {
          error("Unhandled type of part entity %s.".format(other))
          List((None, "Supplier Error"))
        }
      }
    }
    case _ => {
      debug("Current Part is not defined. Generating an empty list of suppliers")
      List((None, "Select a Part"))
    }
  })

  /**
   * The current supplier of the part
   */
  val currentSupplierCell = ValueCell[Option[Supplier]](None)

  /**
   * Find all the suppliers who provide a part
   */
  private def suppliedBy(partId: ObjectId): List[Supplier] = Supplier where (_.suppliedParts.subfield(_.part) eqs partId) orderAsc (_.supplierName) fetch

  /**
   * Calculated value of the base cost of the currently selected part
   *
   * The cost is derived from the
   */
  private val currentPartBaseCostCell = currentPartCell.lift(currentSupplierCell)((_, _) match {
    case (Some(p), Some(s)) => s.suppliedParts.get filter (_.part.get == p.id.get) match {
      case Nil => {
        error("Supplier %s does not supply part with id %s".format(s.supplierName.get, p.id.get))
        0.0d
      }
      case head :: tail => head.suppliedCost.get
    }
    case _ => {
      debug("Either part or supplier not set")
      0.0d
    }
  })

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

  /**
   * Text representation of the current supplier's name
   */
  val supplierText = currentSupplierCell.lift(_ match {
    case Some(s) => s.supplierName.get
    case _ => "No Supplier Set"
  })

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
    debug("Adding %d of %s with a price of %.2f and a %d markup".format(quantityCell.get, currentPart, currentPartBaseCostCell.get, markupCell.get))
    (currentPart, quantityCell.get) match {
      case (None, _) => // do nothing
      case (Some(part), q) if q <= 0 => {
        removeItem(part)
        resetPartQuantityAndSupplier
      }
      case (Some(part), q) => {
        val partCost = currentPartBaseCostCell.get
        val markupValue = markupCell.get.toDouble / 100.0
        addOrUpdateLineItem(partCost, markupValue, part, q)
        resetPartQuantityAndSupplier
      }
    }
  }

  private def resetPartQuantityAndSupplier: Unit = {
    currentSupplierCell set None
    currentPartCell set None
    quantityCell set 0
  }

  /**
   * Get the value of the current part from the holder
   *
   * @return an Option[Part] for the currently selected part, or `None` if no part has been selected
   */
  def currentPart = currentPartCell.get

  /**
   * Set the value of the current part in the holder
   */
  def currentPart_=(partOption: Option[MongoRecord[_] with ObjectIdPk[_]]) = currentPartCell.set(partOption)

  /**
   * Set the value of the current part in the holder to a PartKit
   */
  //def currentPart(partOption: Option[PartKit]) = currentPartCell.set(partOption)

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

    debug("Markup is now: %d".format(markupCell.get.toInt))
  }

  def quantity = "%d".format(quantityCell.get)

  def quantity(q: Int) = quantityCell.set(q)

  def suppliers = {
    val s = suppliersForPart.get
    debug("Generates %s for 'suppliers'".format(s.mkString(", ")))
    s
  }

  def supplier(supplier: Option[Supplier]) = currentSupplierCell.set(supplier)

  def supplier = {
    val s = currentSupplierCell.get
    debug("Supplier is %s".format(s))
    s
  }
}