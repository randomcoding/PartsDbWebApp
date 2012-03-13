/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util

import scala.xml.NodeSeq

import org.joda.time.DateTime

import uk.co.randomcoding.partsdb.core.part.{ PartCost, Part }
import uk.co.randomcoding.partsdb.lift.util._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object PartCostDisplay extends TabularEntityDisplay {

  override val addEditColumn = false
  override val addDisplayColumn = false
  override type EntityType = PartCost

  override val rowHeadings = List("Part", "Cost", "Last Updated", "Supplier Part No.")

  def displayEntity(entity: PartCost, editLink: Boolean = false, displayLink: Boolean = false): NodeSeq = {
    <td>{ displayPartName(entity) }</td>
    <td>{ "£%.2f".format(entity.suppliedCost.get) }</td>
    <td>{ displayDate(entity) }</td>
    <td>{ entity.supplierPartNumber.get }</td> //++ emptyEditAndDisplayCells
  }

  private def displayPartName(partCost: PartCost) = {
    Part findById partCost.part.get match {
      case Some(p) => p.partName.get
      case _ => "Unknown Part Id: %s".format(partCost.part.get)
    }
  }

  private def displayDate(partCost: PartCost) = {
    val date = partCost.lastSuppliedDate.get
    new DateTime(date).toString("dd/MM/yyyy")
  }
}