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
import uk.co.randomcoding.partsdb.core.system.SystemData

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
   * The total computed base cost of the line items, before tax
   *
   * Abstract, required to be defined by subclasses.
   */
  def preTaxTotal: Cell[Double]

  /**
   * This contains the total value of all the line items before tax and without carriage
   *
   * Abstract, required to be defined by subclasses.
   */
  val lineItemsSubTotalCell: Cell[Double]

  /**
   * Container for the value of the carriage associated with this document
   */
  val carriageCell = ValueCell[Double](DEFAULT_CARRIAGE)

  /**
   * The tax rate. Set to value stored in the system data
   */
  val taxRate = ValueCell(SystemData.vatRate)

  /**
   * The computed value of the amount of tax for the quote
   */
  private lazy val tax = preTaxTotal.lift(taxRate)(_ * _)

  /**
   * Calculated total cost of all line items
   */
  private lazy val total = preTaxTotal.lift(tax)(_ + _)

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
   * If the input fails to parse as a double then the carriage is not updated
   */
  def carriage_=(carriageString: String) = {
    asDouble(carriageString) match {
      case Full(value) => carriageCell.set(value)
      case _ => error("Unable to parse %s as numeric value. Not updating carriage".format(carriageString))
    }
  }

  /**
   * Setter for the carriage call
   */
  def carriage_=(amount: Double) = carriageCell.set(amount)

  /**
   * @return The value of the carriage rendered as a currency string
   */
  def carriage = carriageCell.lift("£%.2f".format(_))

  /**
   * @return The value of the `carriage` cell as a formatted string without currency
   */
  def carriageText = "%.2f".format(carriageCell.get)

  /**
   * @return The value of the carriage cell as a Double
   */
  def carriageValue = carriageCell.get
}