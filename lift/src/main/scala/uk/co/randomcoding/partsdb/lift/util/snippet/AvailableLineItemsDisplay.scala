/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet

import uk.co.randomcoding.partsdb.core.document.LineItem
import net.liftweb.util.CssSel
import net.liftweb.util.Helpers._
import uk.co.randomcoding.partsdb.core.part.Part
import scala.xml.NodeSeq
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import scala.xml.Text

/**
 * Renders a collection of [[uk.co.randomcoding.partsdb.core.document.LineItem]]s with checkboxes that can be used
 * to indicate whether or not the item is ''selected'' or not.
 *
 * The operation performed on selection change is defined by the abstract function
 * [[uk.co.randomcoding.partsdb.lift.util.snippet.AvailableLineItemsDisplay#checkBoxSelected(Boolean, LineItem)]].
 *
 * This snippet expects the html template ''_available_line_items.html'' to be used.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait AvailableLineItemsDisplay {
  /**
   * Perform actual rendering of the available line items
   *
   * @param availableLineItems The [[uk.co.randomcoding.partsdb.core.document.LineItem]]s to be rendered as available for selection
   *
   *  @return The `CssSel` transformation of the element with the id ''availableLineItems'' into the selectable line items
   */
  def renderAvailableLineItems(availableLineItems: Seq[LineItem]): CssSel = "#availableLineItems *" #> renderAvailableItems(availableLineItems)

  /**
   * Abstract function called when a checkbox for a line item is selected.
   *
   * This allows update of other items on the selection event
   *
   * @param selected The ''selected'' state of the checkbox. true => selected and false => not selected
   * @param line The [[uk.co.randomcoding.partsdb.core.document.LineItem]] that is associated with this checkbox
   */
  def checkBoxSelected(selected: Boolean, line: LineItem)

  private[this] def renderAvailableItems(availableLineItems: Seq[LineItem]) = availableLineItems map (line => {
    val partName = Part findById line.partId.get match {
      case Some(p) => p.partName.get
      case _ => "No Part"
    }

    "#selected" #> styledAjaxCheckbox(false, checkBoxSelected(_, line)) &
      "#partName" #> Text(partName) &
      "#partQuantity" #> Text("%d".format(line.quantity.get)) &
      "#totalLineCost" #> Text("£%.2f".format(line.lineCost))
  })
}