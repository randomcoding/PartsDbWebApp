/**
 *
 */
package uk.co.randomcoding.partsdb.lift.model.document

import org.bson.types.ObjectId

import com.foursquare.rogue.Rogue._

import uk.co.randomcoding.partsdb.core.document.LineItem
import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.core.supplier.Supplier

import net.liftweb.common.{ Logger, Full }
import net.liftweb.util.Helpers._
import net.liftweb.util.{ ValueCell, Cell }

/**
 * Encapsulates the basic data for the total costs of a [[uk.co.randomcoding.partsdb.core.document.Document]].
 *
 * This contains references & calculations for the total, pre-tax subtotal, carriage costs etc of a [[uk.co.randomcoding.partsdb.core.document.Document]].
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait DocumentDataHolder extends Logger {
  /**
   * The default markup rate for new lines
   */
  private val DEFAULT_CARRIAGE = 0.0d

  /**
   * Container for the value of the carriage associated with this document
   */
  val carriageCell = ValueCell[Double](DEFAULT_CARRIAGE)

  /**
   * The total computed base cost of the line items, before tax
   */
  def preTaxTotal: Cell[Double]

  /**
   * The tax rate. Set to 0.2 (20%)
   */
  val taxRate = ValueCell(0.2d)

  /**
   * The computed value of the amount of tax for the quote
   */
  private lazy val tax = preTaxTotal.lift(taxRate)(_ * _)

  /**
   * Calculated total cost of all line items
   */
  private lazy val total = preTaxTotal.lift(tax)(_ + _)

  /**
   * This contains the total value of all the line items before tax and without carriage
   */
  val lineItemsSubTotalCell: Cell[Double]

  // Values for display in the GUI

  /**
   * Display the pre-tax total for the line items only in £0.00 format.
   * Does not include the carriage cost
   *
   * Suitable for use as:
   * {{{
   * WiringUI.asText(holder.subTotal)
   * }}}
   */
  final def subTotal = lineItemsSubTotalCell.lift("£%.2f".format(_))

  /**
   * The amount of vat for all the current line items
   *
   * Suitable for use as:
   * {{{
   * WiringUI.asText(holder.vatAmount)
   * }}}
   */
  lazy val vatAmount = tax.lift("£%.2f".format(_))

  /**
   * The computed total of the line items plus tax in £0.00 format
   *
   * Suitable for use as:
   * {{{
   * WiringUI.asText(holder.totalCost)
   * }}}
   */
  lazy val totalCost = total.lift("£%.2f".format(_))

  /**
   * Setter for the carriage call that parses a String input.
   *
   * If the input fails to parse as a double then the cariage is not updated
   */
  def carriage(carriageString: String) = {
    debug("Setting carriage to: %s".format(carriageString))
    asDouble(carriageString) match {
      case Full(value) => carriageCell.set(value)
      case _ => // do nothing
    }
  }

  /**
   * Setter for the carriage call
   */
  def carriage(amount: Double) = carriageCell.set(amount)

  /**
   * @return The value of the carriage rendered as a currency string
   */
  val carriage = carriageCell.lift("£%.2f".format(_))

  /**
   * @return The value of the `carriage` cell as a formatted string without currency
   */
  def carriageText = "%.2f".format(carriageCell.get)

  /**
   * @return The value of the carriage cell as a Double
   */
  def carriageValue = carriageCell.get
}