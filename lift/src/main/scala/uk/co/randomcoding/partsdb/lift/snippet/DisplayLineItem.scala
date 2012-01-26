/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import scala.xml.NodeSeq

import uk.co.randomcoding.partsdb.lift.util.EntityDisplay
import uk.co.randomcoding.partsdb.core.document.LineItem
import uk.co.randomcoding.partsdb.db.DbAccess
import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.core.id.Identifier

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object DisplayLineItem extends EntityDisplay with DbAccess {
  override type EntityType = LineItem

  override val rowHeadings = List("Line No.", "Part", "Quantity", "Base Price", "Markup", "Total")

  override def displayEntity(lineItem: LineItem): NodeSeq = {
    part(lineItem.partId) match {
      case Some(p) => {
        <tr>
          <td>{ lineItem.lineNumber }</td>
          <td>{ /*p.partName*/ }</td>
          <td>{ lineItem.quantity }</td>
          <td>{ "£%.2f".format(lineItem.basePrice) }</td>
          <td>{ "%.0f".format(lineItem.markup * 100) + "%" }</td>
          <td>{ /*"£" + totalCost(lineItem, p)*/ }</td>
        </tr>
      }
      case _ => emptyRow
    }
  }

  private def totalCost(lineItem: LineItem, part: Part) = "%.2f".format(lineItem.lineCost)

  private def part(partId: Identifier): Option[Part] = None // getOne[Part]("partId", partId)
}