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
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds.SetHtml
import scala.xml.Attribute
import scala.xml.Null
import net.liftweb.common.Logger

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

  /**
   * Perform actual rendering of the available line items
   *
   * @param availableLineItems The [[uk.co.randomcoding.partsdb.core.document.LineItem]]s to be rendered as available for selection
   *
   *  @return The `CssSel` transformation of the element with the id ''availableLineItems'' into the selectable line items
   */
  def renderAvailableLineItems(availableLineItems: Seq[LineItem]): CssSel = "#availableLineItems *" #> renderAvailableItems(availableLineItems)

  /**
   * Refresh the contents of the `availableLineItems` elements. Replacing it with the provided items
   *
   * @param availableLineItems The line items that are to be rendered as currently available
   *
   * @return The `JsCmd` that will re-render the html
   */
  def refreshAvailableLineItems(availableLineItems: Seq[LineItem]): JsCmd = SetHtml("availableLineItems", refreshItems(availableLineItems))

  /*
   * TODO: This is a nasty, nasty hack and a proper way of doing this needs to be found.
   */
  private[this] def refreshItems(items: Seq[LineItem]): NodeSeq = {
    val elements = items flatMap (item => {
      val checkbox = <div>{ styledAjaxCheckbox(false, checkBoxSelected(_, item)) }</div> % idAttribute("selected") % classAttribute("column span-1")
      val partNameDisplay = <div class="column span-4">
                              <span class="form-label">{ Text("Part:  ") }</span>
                              <span id="partName">{ Text(partName(item)) }</span>
                            </div>

      val quantityDisplay = <div class="column span-4">
                              <span class="form-label">{ Text("Quantity:  ") }</span>
                              <span id="partQuantity">{ Text("%d".format(item.quantity.get)) }</span>
                            </div>

      val costDisplay = <div class="column span-4">
                          <span class="form-label">{ Text("Cost:  ") }</span>
                          <span id="totalLineCost">{ Text("%.2f".format(item.lineCost)) }</span>
                        </div>

      checkbox ++ partNameDisplay ++ quantityDisplay ++ costDisplay
    })

    debug("Generated elements: %s".format(elements.mkString("[", ", ", "]")))
    <div>{ elements }</div> % idAttribute("availableLineItems")
  }

  private[this] def partName(item: LineItem): String = Part findById item.partId.get match {
    case Some(p) => p.partName.get
    case _ => "No Part"
  }

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

  private[this] def idAttribute(id: String) = Attribute(None, "id", Text(id), Null)

  private[this] def classAttribute(attr: String) = Attribute(None, "class", Text(attr), Null)
}