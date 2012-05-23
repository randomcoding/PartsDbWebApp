/**
 *
 */
package uk.co.randomcoding.partsdb.lift.model.document

import uk.co.randomcoding.partsdb.core.document.LineItem
import uk.co.randomcoding.partsdb.core.part.Part
import net.liftweb.util.ValueCell
import net.liftweb.common.Logger
import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.field.ObjectIdPk
import uk.co.randomcoding.partsdb.core.part.PartKit

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait LineItemsDataHolder extends Logger {
  /**
   * Holder for the current line items
   */
  val lineItemsCell = ValueCell[List[LineItem]](Nil)

  /**
   * The pre-tax total of the line items' values without carriage
   */
  val itemsPreTaxSubTotal = lineItemsCell.lift(_.foldLeft(0.0d)(_ + _.lineCost))

  /**
   * Add a new [[uk.co.randomcoding.partsdb.core.document.LineItem]] to the currently stored [[uk.co.randomcoding.partsdb.core.document.LineItem]]s
   */
  def addLineItem(lineItem: LineItem): Unit = {
    lineItemsCell.atomicUpdate(items => items.find(_.partId.get == lineItem.partId.get) match {
      case None => items :+ lineItem
      case Some(item) => {
        debug("Line Items already contains an item for part with id %S".format(item.partId.get))
        items
      }
    })
  }

  /**
   * Remove a [[uk.co.randomcoding.partsdb.core.document.LineItem]] from the currently stored [[uk.co.randomcoding.partsdb.core.document.LineItem]]s
   *
   * @param lineItem The [[uk.co.randomcoding.partsdb.core.document.LineItem]] to remove
   */
  def removeLineItem(lineItem: LineItem): Unit = lineItemsCell.atomicUpdate(_.filterNot(_ == lineItem))

  /**
   * Remove a [[uk.co.randomcoding.partsdb.core.document.LineItem]] from the currently stored [[uk.co.randomcoding.partsdb.core.document.LineItem]]s identified by
   * the [[uk.co.randomcoding.partsdb.core.part.Part]] that the line item is for.
   *
   * @param part The [[uk.co.randomcoding.partsdb.core.part.Part]] that identifies the [[uk.co.randomcoding.partsdb.core.document.LineItem]]s to remove
   */
  def removeItem(part: MongoRecord[_] with ObjectIdPk[_]) = {
    lineItemsCell.atomicUpdate(_.filterNot(_.partId.get == part.id.get))
    renumberLines
  }

  /**
   * Add a new [[uk.co.randomcoding.partsdb.core.document.LineItem]]. Alternatively, if there is already a [[uk.co.randomcoding.partsdb.core.document.LineItem]] for the
   * specified [[uk.co.randomcoding.partsdb.core.part.Part]] then that item will be updated instead.
   *
   * @param partCost The cost of the part in the line
   * @param markup The percentage (as a Double, 0.0 - 1.0)
   * @param part The [[uk.co.randomcoding.partsdb.core.part.Part]] for the line
   * @param quantity The number of parts in this line.
   */
  def addOrUpdateLineItem(partCost: Double, markup: Double, part: MongoRecord[_] with ObjectIdPk[_], quantity: Int) {
    lineItemsCell.atomicUpdate(items => items.find(_.partId.get == part.id.get) match {
      case Some(lineItem) => {
        val newItem: LineItem = part match {
          case p: Part => LineItem.create(lineItem.lineNumber.get, p, quantity, partCost, markup)
          case pk: PartKit => LineItem.create(lineItem.lineNumber.get, pk, quantity, partCost, markup)
        }
        removeLineItem(lineItem)
        addLineItem(newItem)
        lineItems
      }
      case _ => {
        val lineItem = part match {
          case p: Part => LineItem.create(items.size, p, quantity, partCost, markup)
          case pk: PartKit => LineItem.create(items.size, pk, quantity, partCost, markup)
        }

        items :+ lineItem
      }
    })
  }

  /**
   * Renumbers the line items, starting from 0.
   *
   * This preserves the current order, only the line numbers are changed.
   */
  private def renumberLines = lineItemsCell.atomicUpdate(items => {
    var index = 0
    items sortBy (_.lineNumber.get) map (item => {
      val newItem = item.lineNumber(index)
      index += 1
      newItem
    })
  })

  /**
   * Update the quantity, cost and markup of a line item
   *
   * @todo This does not seem to trigger a refresh of the cell and therefore does not cause a `WiringUI` update event to fire
   */
  private[this] val updateLineItem = (li: LineItem, quant: Int, cost: Double, markupValue: Double) => li.quantity(quant).basePrice(cost).markup(markupValue)

  /**
   * Gets the current line items, sorted by line number
   */
  def lineItems = lineItemsCell.get.sortBy(_.lineNumber.get)
}