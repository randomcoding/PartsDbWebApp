/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util

import scala.xml.NodeSeq
import uk.co.randomcoding.partsdb.core.part.PartCost
import uk.co.randomcoding.partsdb.core.part.Part
import org.joda.time.DateTime

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object PartCostDisplay extends EntityDisplay {

  override type EntityType = PartCost

  override val rowHeadings = List("Part", "Cost", "Last Updated")

  def displayEntity(entity: PartCost): NodeSeq = {
    <td>{ displayPartName(entity) }</td>
    <td>{ "£%.2f".format(entity.suppliedCost.get) }</td>
    <td>{ displayDate(entity) }</td> ++ <td></td>
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