/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import uk.co.randomcoding.partsdb.lift.util.EntityDisplay
import uk.co.randomcoding.partsdb.core.document.LineItem
import scala.xml.NodeSeq
import uk.co.randomcoding.partsdb.db.DbAccess
import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.core.id.Identifier

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object DisplayLineItem extends EntityDisplay with DbAccess {
  override type EntityType = LineItem

  //TODO: Come up with some way of adding a part with a part cost from the list, taken a copy of the original code
  /*  override val rowHeadings = List("Line No.", "Part", "Quantity", "Unit Price", "Total")

  override def displayEntity(lineItem: LineItem): NodeSeq = {
    part(lineItem.partId) match {
      case Some(p) => {
        <tr>
          <td>{ lineItem.lineNumber }</td>
          <td>{ p.partName }</td>
          <td>{ lineItem.quantity }</td>
          <td>{ "%.2f".format(p.partCost) }</td>
          <td>{ totalCost(lineItem, p) }</td>
        </tr>
      }
      case _ => emptyRow
    }
  }
  */
  override val rowHeadings = List("Line No.", "Part", "Quantity")

  override def displayEntity(lineItem: LineItem): NodeSeq = {
    part(lineItem.partId) match {
      case Some(p) => {
        <tr>
          <td>{ lineItem.lineNumber }</td>
          <td>{ p.partName }</td>
          <td>{ lineItem.quantity }</td>
        </tr>
      }
      case _ => emptyRow
    }
  }

  //TODO: Come up with some way of calculating total cost
  //private def totalCost(lineItem: LineItem, part: Part) = "%.2f".format(lineItem.quantity * part.partCost)

  private def part(partId: Identifier) = getOne[Part]("partId", partId)
}