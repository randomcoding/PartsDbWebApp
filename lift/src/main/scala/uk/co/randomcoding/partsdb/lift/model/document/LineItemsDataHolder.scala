/**
 *
 */
package uk.co.randomcoding.partsdb.lift.model.document

import uk.co.randomcoding.partsdb.core.document.LineItem
import uk.co.randomcoding.partsdb.core.part.Part
import net.liftweb.util.ValueCell

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait LineItemsDataHolder {
  /**
   * Holder for the current line items
   */
  val lineItemsCell = ValueCell[List[LineItem]](Nil)

  /**
   * The pre-tax total of the line items' values without carriage
   */
  val itemsPreTaxSubTotal = lineItemsCell.lift(_.foldLeft(0.0d)(_ + _.lineCost))

  def addLineItem(lineItem: LineItem): Unit = {
    lineItemsCell.atomicUpdate(items => items.find(_.partId.get == lineItem.partId.get) match {
      case None => items :+ lineItem
      case Some(item) => items
    })
  }

  def removeLineItem(lineItem: LineItem): Unit = lineItemsCell.atomicUpdate(_.filterNot(_ == lineItem))

  def removeItem(part: Part) = {
    lineItemsCell.atomicUpdate(_.filterNot(_.partId.get == part.id.get))
    renumberLines
  }

  def addOrUpdateLineItem(partCost: Double, markup: Double, part: Part, q: Int) {
    lineItemsCell.atomicUpdate(items => items.find(_.partId.get == part.id.get) match {
      case Some(lineItem) => items.map(li => {
        li.partId.get == part.id.get match {
          case true => updateLineItem(li, q, partCost, markup)
          case false => li
        }
      })
      case _ => items :+ LineItem.create(items.size, part, q, partCost, markup)
    })
  }

  private def renumberLines = lineItemsCell.atomicUpdate(items => {
    var index = 0
    items sortBy (_.lineNumber.get) map (item => {
      val newItem = item.lineNumber(index)
      index += 1
      newItem
    })
  })

  private val updateLineItem = (li: LineItem, quant: Int, cost: Double, markupValue: Double) => li.quantity(quant).basePrice(cost).markup(markupValue)

  /**
   * Gets the current line items, sorted by line number
   */
  def lineItems = lineItemsCell.get.sortBy(_.lineNumber.get)
}