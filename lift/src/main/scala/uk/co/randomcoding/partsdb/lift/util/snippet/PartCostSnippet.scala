/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet

import scala.Array.canBuildFrom
import scala.xml.{ Text, Null, NodeSeq, Attribute }

import org.joda.time.DateTime

import com.foursquare.rogue.Rogue._

import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.core.part.{ PartCost, Part }
import uk.co.randomcoding.partsdb.core.supplier.Supplier
import uk.co.randomcoding.partsdb.lift.util.DateHelpers.dateString
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet._
import uk.co.randomcoding.partsdb.lift.util._

import net.liftweb.common.{ Logger, Full }
import net.liftweb.http.js.JsCmds.{ SetHtml, Replace, Noop }
import net.liftweb.http.js.JsCmd.unitToJsCmd
import net.liftweb.http.js.JsCmd
import net.liftweb.http.SHtml
import net.liftweb.util.Helpers._

/**
 * Snippet to handle the processing of adding, removing and displaying [[uk.co.randomcoding.partsdb.core.part.PartCost]]s for a
 * [[uk.co.randomcoding.partsdb.core.supplier.Supplier]]
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait PartCostSnippet extends ErrorDisplay with DataValidation with Logger {

  var currentPartCosts: List[PartCost]

  private val parts = Part where (_.id exists true) orderDesc (_.partName) fetch
  val partsSelect = (None, "Select Part") :: (parts map ((p: Part) => (Some(p), p.partName.get)))

  private var currentPart: Option[Part] = None
  private var currentPartCost: Double = 0.0d

  val defaultDate = new DateTime(1970, 1, 1, 12, 00)

  private var currentPartLastSuppliedDate: DateTime = defaultDate
  private var supplierPartNumber = ""

  def renderAddPartCost() = {
    "#partSelect" #> styledAjaxObjectSelect(partsSelect, currentPart, updateAjaxValue[Option[Part]](currentPart = _, currentPartUpdated())) &
      "#costEntry" #> costEntryContent() &
      "#lastSuppliedEntry" #> lastSuppliedDateContent() &
      "#addPartCost" #> styledAjaxButton("Add / Update", addPartCost) &
      "#supplierPartNumber" #> supplierPartNumberContent() &
      "#removePartCost" #> styledAjaxButton("Remove", removePartCost)
  }

  private[this] def currentPartUpdated() = {
    currentPart match {
      case Some(pt) => {
        currentPartCosts find (_.part.get == pt.id.get) match {
          case Some(p) => {
            updateCurrentPartCost("%.2f".format(p.suppliedCost.get))
            updateCurrentPartLastSuppliedDate(dateString(new DateTime(p.lastSuppliedDate.get)))
            updateSupplierPartNumber(p.supplierPartNumber.get)
          }
          case _ => {
            updateCurrentPartCost("0.00")
            updateCurrentPartLastSuppliedDate(dateString(defaultDate))
            updateSupplierPartNumber("")

          }
        }
        SHtml.ajaxInvoke(() => refreshCostEntry() & refreshLastSuppliedDate() & refreshSupplierPartNumber())._2.cmd
      }
      case _ => Noop
    }
  }

  def renderCurrentPartCosts() = "#currentPartCosts" #> PartCostDisplay(currentPartCosts, false, false)

  def addSupplier(name: String, contacts: ContactDetails, address: Address, currentPartCosts: List[PartCost]): Option[Supplier] = {
    Supplier.add(name, contacts, address, currentPartCosts)
  }

  def modifySupplier(supplier: Supplier, newName: String, contacts: ContactDetails, address: Address, currentPartCosts: List[PartCost]): JsCmd = {
    Supplier.modify(supplier.id.get, newName, contacts, address, currentPartCosts, supplier.notes.get)
  }

  private[this] def updateCurrentPartCost(cost: String) = currentPartCost = asDouble(cost) match {
    case Full(d) => d
    case _ => 0.0d
  }

  private[this] def updateCurrentPartLastSuppliedDate(dateString: String) = {
    debug("Updating last supplied date to string %s".format(dateString))
    currentPartLastSuppliedDate = {
      val dateParts = dateString split ("/") map (asInt(_) openOr -1)
      if (dateParts contains -1) {
        displayError("Cannot create Date from: %s".format(dateString))
        defaultDate
      }
      else {
        debug("Using d: %d m: %d y: %d".format(dateParts(0), dateParts(1), dateParts(2)))
        new DateTime(dateParts(2), dateParts(1), dateParts(0), 12, 0)
      }
    }
  }

  private[this] def updateSupplierPartNumber(partNumber: String) = supplierPartNumber = partNumber

  private[this] def refreshPartCostDisplay(): JsCmd = {
    debug("Updating current parts to: %s".format(currentPartCosts.mkString("\n")))
    SetHtml("currentPartCosts", PartCostDisplay(currentPartCosts sortBy (partCost => Part.findById(partCost.part.get) match {
      case Some(p) => p.partName.get
      case _ => "zzzzzzz"
    }), false, false))
  }

  private[this] def validations() = Seq(ValidationItem(currentPart, "Current Part"),
    ValidationItem(currentPartCost, "Current Part Cost"),
    ValidationItem(currentPartLastSuppliedDate, "Last Supplied Date"),
    ValidationItem(supplierPartNumber, "Supplier Part Number"))

  private[this] def addPartCost(): JsCmd = {
    clearErrors
    debug("Adding a part cost")

    validate(validations(): _*) match {
      case Nil => {
        updatePartCosts(PartCost.create(currentPart.get, currentPartCost, currentPartLastSuppliedDate, supplierPartNumber))
        clearErrorsAndRefresh
      }
      case errors => {
        displayErrors(errors: _*)
        Noop
      }
    }
  }

  private[this] def removePartCost(): JsCmd = {
    currentPart isDefined match {
      case true => {
        debug("Current part is defined as: %s".format(currentPart))
        debug("Removing part cost for part: %s from the part costs".format(currentPart))
        clearErrors
        debug("Current Parts initially: %s".format(currentPartCosts.mkString(", ")))
        currentPartCosts = currentPartCosts filterNot (_.part.get == currentPart.get.id.get)
        debug("Current Parts after removal: %s".format(currentPartCosts.mkString(", ")))
      }
      case false => displayError("Please select a Part to remove")
    }

    clearErrorsAndRefresh
  }

  private[this] def clearErrorsAndRefresh(): JsCmd = {
    clearErrors &
      refreshPartCostDisplay
  }

  private[this] def resetCurrentPartCost() = {
    currentPart = None
    currentPartCost = 0.0d
    currentPartLastSuppliedDate = defaultDate
  }

  private[this] def updatePartCosts(partCost: PartCost) = {
    debug("Adding %s to current part costs".format(partCost))

    currentPartCosts find (_.part.get == partCost.part.get) match {
      case Some(pc) => {
        debug("Found part id %s in current part costs".format(pc.part.get))
        // create copy of part with same id and replace entry in list
        val newPartCost = PartCost.create(Part.findById(partCost.part.get).get, partCost.suppliedCost.get, new DateTime(partCost.lastSuppliedDate.get), partCost.supplierPartNumber.get)
        currentPartCosts = newPartCost :: currentPartCosts.filterNot(_.part.get == pc.part.get)
      }
      case _ => {
        debug("Not Found part id %s in current part costs".format(partCost.part.get))
        currentPartCosts = partCost :: currentPartCosts
      }
    }

    debug("Current Part costs are now: %s".format(currentPartCosts.mkString("\n")))
  }

  private[this] def costEntryContent(): NodeSeq = styledAjaxText("%.2f".format(currentPartCost), updateAjaxValue(updateCurrentPartCost(_)))

  private[this] def refreshCostEntry(): JsCmd = Replace("costEntry", <span>{ costEntryContent }</span> % Attribute(None, "id", Text("costEntry"), Null))

  private[this] def lastSuppliedDateContent(): NodeSeq = styledAjaxText(dateString(currentPartLastSuppliedDate), updateAjaxValue(updateCurrentPartLastSuppliedDate(_)))

  private[this] def refreshLastSuppliedDate(): JsCmd = Replace("lastSuppliedEntry", <span>{ lastSuppliedDateContent }</span> % Attribute(None, "id", Text("lastSuppliedEntry"), Null))

  private[this] def supplierPartNumberContent(): NodeSeq = styledAjaxText(supplierPartNumber, updateAjaxValue(supplierPartNumber = _))

  private[this] def refreshSupplierPartNumber(): JsCmd = Replace("supplierPartNumber", <span>{ supplierPartNumberContent }</span> % Attribute(None, "id", Text("supplierPartNumber"), Null))
}
