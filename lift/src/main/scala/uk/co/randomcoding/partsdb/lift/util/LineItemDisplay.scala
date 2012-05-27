/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util

import scala.xml.NodeSeq
import org.bson.types.ObjectId
import com.foursquare.rogue.Rogue._
import uk.co.randomcoding.partsdb.core.document.LineItem
import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.lift.util._
import uk.co.randomcoding.partsdb.core.part.PartKit
import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.field.ObjectIdPk

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object LineItemDisplay extends TabularEntityDisplay {

  override val addEditColumn = false
  override val addDisplayColumn = false

  override type EntityType = LineItem

  override val rowHeadings = List("Line No.", "Part", "Quantity", "Base Price", "Markup", "Total")

  override def displayEntity(lineItem: LineItem, editLink: Boolean = false, displayLink: Boolean = false): NodeSeq = {
    partOrKit(lineItem.partId.get) match {
      case Some(p) => row(lineItem, p)
      case _ => emptyRow
    }
  }

  private def partOrKit(partId: ObjectId): Option[MongoRecord[_] with ObjectIdPk[_]] = Part where (_.id eqs partId) get match {
    case Some(p) => Some(p)
    case _ => PartKit.where(_.id eqs partId).get
  }

  private[this] def row(lineItem: LineItem, p: MongoRecord[_] with ObjectIdPk[_]) = {
    <td>{ lineItem.lineNumber }</td>
    <td>
      { partOrKitName(p) }
    </td>
    <td>{ lineItem.quantity.get }</td>
    <td>{ "£%.2f".format(lineItem.basePrice.get) }</td>
    <td>{ "%.0f%%".format(lineItem.markup.get * 100) }</td>
    <td>{ "£" + totalCost(lineItem) }</td>
  }

  private[this] def totalCost(lineItem: LineItem) = "%.2f".format(lineItem.lineCost)

  private[this] def partOrKitName(p: MongoRecord[_] with ObjectIdPk[_]): String = p match {
    case part: Part => part.partName.get
    case partKit: PartKit => partKit.kitName.get
    case _ => "Unknown Object"
  }

}