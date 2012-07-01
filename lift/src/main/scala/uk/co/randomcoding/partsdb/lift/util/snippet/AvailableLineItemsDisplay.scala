/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet

import scala.xml.{ Text, NodeSeq }

import org.bson.types.ObjectId

import uk.co.randomcoding.partsdb.core.document.LineItem
import uk.co.randomcoding.partsdb.core.part.{ PartKit, Part }
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._

import net.liftweb.common.Logger
import net.liftweb.http.js.JsCmd
import net.liftweb.http.WiringUI
import net.liftweb.util.Helpers._
import net.liftweb.util.Cell

/**
 * Renders a collection of [[uk.co.randomcoding.partsdb.core.document.LineItem]]s with checkboxes that can be used
 * to indicate whether or not the item is ''selected'' or not.
 *
 * The operation performed on selection change is defined by the abstract function
 * [[uk.co.randomcoding.partsdb.lift.util.snippet.AvailableLineItemsDisplay#checkBoxSelected(Boolean,LineItem)]].
 *
 * This snippet expects the html template ''_available_line_items.html'' to be used.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait AvailableLineItemsDisplay extends Logger {

  /**
   * Abstract function called when a checkbox for a line item is selected.
   *
   * This allows update of other items on the selection event
   *
   * @param selected The ''selected'' state of the checkbox. true => selected and false => not selected
   * @param line The [[uk.co.randomcoding.partsdb.core.document.LineItem]] that is associated with this checkbox
   */
  def checkBoxSelected(selected: Boolean, line: LineItem): JsCmd

  private[this] def partName(item: LineItem): String = Part findById item.partId.get match {
    case Some(p) => p.partName.get
    case _ => "No Part"
  }

  /**
   * Use the `WiringUI` to render dynamic line items
   */
  def renderAvailableLineItems(availableItemsCell: Cell[List[LineItem]]) = {
    "#availableLineItems" #> WiringUI.toNode(availableItemsCell)(renderWiringAvailableItems)
  }

  private[this] val renderWiringAvailableItems: (Seq[LineItem], NodeSeq) => NodeSeq = (items, nodes) => {
    val rows = items map (line => {
      <td align="left" style="width: 3em">{ styledAjaxCheckbox(false, checkBoxSelected(_, line), List("style" -> "width: 2em")) }</td>
      <td>{ Text(partOrKitName(line.partId.get)) }</td>
      <td>{ Text("%d".format(line.quantity.get)) }</td>
      <td>{ Text("£%.2f".format(line.lineCost)) }</td>
    })

    val rowsNodes = rows flatMap (row => <tr>
                                           { row }
                                         </tr>)

    <table>
      <tr>
        <th>Selected?</th>
        <th>Item Name</th>
        <th>Quantity</th>
        <th>Cost</th>
      </tr>
      { rowsNodes }
    </table>

  }

  private[this] def partOrKitName(oid: ObjectId) = {
    Part.findById(oid) match {
      case Some(p) => p.partName.get
      case _ => PartKit.findById(oid) match {
        case Some(pk) => pk.kitName.get
        case _ => "No Part or PartKit"
      }
    }
  }
}