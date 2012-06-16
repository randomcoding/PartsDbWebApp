/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util

import scala.xml.NodeSeq.seqToNodeSeq
import scala.xml.{ NodeSeq, Text }

import org.bson.types.ObjectId

import com.foursquare.rogue.Rogue._

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.field.ObjectIdPk

import uk.co.randomcoding.partsdb.core.document.LineItem
import uk.co.randomcoding.partsdb.core.part.{ PartKit, Part, PartCost }
import uk.co.randomcoding.partsdb.core.supplier.Supplier

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object LineItemDisplay extends TabularEntityDisplay {

  override val addEditColumn = false
  override val addDisplayColumn = false

  override type EntityType = LineItem

  override val rowHeadings = List("Line No.", "Part", "Supplier Info", "Quantity", "Base Price", "Markup", "Total")

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
    <td>{ partOrKitName(p) }</td>
    <td>{ supplierPartInfo(lineItem.partSupplier.get, p) }</td>
    <td>{ lineItem.quantity.get }</td>
    <td>{ "£%.2f".format(lineItem.basePrice.get) }</td>
    <td>{ "%.0f%%".format(lineItem.markup.get * 100) }</td>
    <td>{ "£" + totalCost(lineItem) }</td>
  }

  private[this] def supplierPartInfo(supplierId: ObjectId, partOrKit: MongoRecord[_] with ObjectIdPk[_]) = {
    partOrKit match {
      case part: Part => Supplier.findById(supplierId) match {
        case Some(s) => <div>
                          <strong>Supplier:</strong>{ s.supplierName.get }<br/>
                          <strong>Supplier Part Id:</strong>{ supplierPartId(s, part) }<br/>
                          <strong>Part Mod Id:</strong>{ part.modId.get.getOrElse("No MoD Id") }
                        </div>
        case _ => Text("No Supplier Identified")
      }
      case kit: PartKit => Text("C.A.T.9")
    }
  }

  private[this] def supplierPartId(supplier: Supplier, part: Part): String = {
    supplier.suppliedParts.get.find(_.part.get == part.id.get) match {
      case Some(partCost) => partCost.supplierPartNumber.get
      case _ => "No Part Cost for Part %s in Supplier %s record".format(part.partName.get, supplier.supplierName.get)
    }

  }

  private[this] def totalCost(lineItem: LineItem) = "%.2f".format(lineItem.lineCost)

  private[this] def partOrKitName(p: MongoRecord[_] with ObjectIdPk[_]): String = p match {
    case part: Part => part.partName.get
    case partKit: PartKit => partKit.kitName.get
    case _ => "Unknown Object"
  }

}