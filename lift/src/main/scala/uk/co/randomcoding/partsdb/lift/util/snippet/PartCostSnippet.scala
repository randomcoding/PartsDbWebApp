/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet

import scala.Array.canBuildFrom

import org.joda.time.DateTime

import com.foursquare.rogue.Rogue._

import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.core.part.PartCost
import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.core.supplier.Supplier
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet._
import uk.co.randomcoding.partsdb.lift.util._

import net.liftweb.common.{Logger, Full}
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.http.js.JsCmd.unitToJsCmd
import net.liftweb.http.js.JsCmd
import net.liftweb.util.Helpers._

/**
 * Snippet to handle the processing of adding, removing and displaying [[uk.co.randomcoding.partsdb.core.part.PartCost]]s for a
 * [[uk.co.randomcoding.partsdb.core.supplier.Supplier]]
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait PartCostSnippet extends ErrorDisplay with Logger {

  var currentPartCosts: List[PartCost]

  private val parts = Part where (_.id exists true) orderDesc (_.partName) fetch
  val partsSelect = (None, "Select Part") :: (parts map ((p: Part) => (Some(p), p.partName.get)))

  private var currentPart: Option[Part] = None
  private var currentPartCost: Double = 0.0d

  val defaultDate = new DateTime(1970, 1, 1, 12, 00)

  private var currentPartLastSuppliedDate: DateTime = defaultDate
  private val dateFormat = "dd/MM/yyyy"
  private def dateString(date: DateTime) = date.toString("dd/MM/yyyy")

  def renderAddPartCost() = {
    "#partSelect" #> styledAjaxObjectSelect(partsSelect, currentPart, updateAjaxValue[Option[Part]](currentPart = _)) &
      "#costEntry" #> styledAjaxText("%.2f".format(currentPartCost), updateAjaxValue(updateCurrentPartCost(_))) &
      "#lastSuppliedEntry" #> styledAjaxText(dateString(currentPartLastSuppliedDate), updateAjaxValue(updateCurrentPartLastSuppliedDate(_))) &
      "#addPartCost" #> styledAjaxButton("Add / Update", addPartCost) &
      "#removePartCost" #> styledAjaxButton("Remove", removePartCost)
  }

  def renderCurrentPartCosts() = {
    "#currentPartCosts" #> PartCostDisplay.displayTable(currentPartCosts)
  }

  def addSupplier(name: String, contacts: ContactDetails, address: Address, currentPartCosts: List[PartCost]): Option[Supplier] = {
    /*currentPartCosts map (PartCost add _) contains (None) match {
      case true => error("Did not add all part costs to the database. Please check")
      case false => // all ok
    }*/

    Supplier.add(name, contacts, address, currentPartCosts)
  }

  def modifySupplier(supplier: Supplier, newName: String, contacts: ContactDetails, address: Address, currentPartCosts: List[PartCost]): JsCmd = {
    Supplier.modify(supplier.id.get, newName, contacts, address, currentPartCosts, supplier.notes.get)
  }

  private def updateCurrentPartCost(cost: String) = currentPartCost = asDouble(cost) match {
    case Full(d) => d
    case _ => 0.0d
  }

  private def updateCurrentPartLastSuppliedDate(dateString: String) = {
    debug("Updating last supplied date to string %s".format(dateString))
    currentPartLastSuppliedDate = {
      val dateParts = dateString split ("/") map (asInt(_) openOr -1)
      if (dateParts contains -1) {
        displayError("Error Location", "Cannot create Date from: %s".format(dateString))
        defaultDate
      }
      else {
        debug("Using d: %d m: %d y: %d".format(dateParts(0), dateParts(1), dateParts(2)))
        new DateTime(dateParts(2), dateParts(1), dateParts(0), 12, 0)
      }
    }
  }

  private[this] def refreshPartCostDisplay(): JsCmd = {
    debug("Updating current parts to: %s".format(currentPartCosts.mkString("\n")))
    SetHtml("currentPartCosts", PartCostDisplay.displayTable(currentPartCosts sortBy (partCost => Part.findById(partCost.part.get) match {
      case Some(p) => p.partName.get
      case _ => "zzzzzzz"
    })))
  }

  private[this] def addPartCost(): JsCmd = {
    clearErrors
    debug("Adding a part cost")
    (currentPart, currentPartCost, currentPartLastSuppliedDate) match {
      case (_, cost: Double, _) if (cost <= 0.0d) => displayError("costErrorId", "Please enter a cost")
      case (_, _, date: DateTime) if (date.equals(defaultDate) || date.isAfter(DateTime.now)) => displayError("dateErrorId", "Date is not valid")
      case (None, _, _) => displayError("partErrorId", "Please select a part")
      case (Some(p), cost: Double, date: DateTime) if (cost > 0.0 && date != defaultDate) => updatePartCosts(PartCost.create(p, cost, date))
      case (opt, cost, date) => error("Unhandled options supplied (%s, %.2f, %s)".format(opt, cost, dateString(date)))
    }

    clearErrorsAndRefresh
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
      case false => displayError("ErrorMessages", "Please select a Part to remove")
    }

    clearErrorsAndRefresh
  }

  private[this] def clearErrorsAndRefresh(): JsCmd = {
    //resetCurrentPartCost &
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
        val newPartCost = PartCost.create(Part.findById(partCost.part.get).get, partCost.suppliedCost.get, new DateTime(partCost.lastSuppliedDate.get))
        currentPartCosts = newPartCost :: currentPartCosts.filterNot(_.part.get == pc.part.get)
      }
      case _ => {
        debug("Not Found part id %s in current part costs".format(partCost.part.get))
        currentPartCosts = partCost :: currentPartCosts
      }
    }

    debug("Current Part costs are now: %s".format(currentPartCosts.mkString("\n")))
  }

  /*private[this] def updatePartCosts(partCosts: List[PartCost]): List[PartCost] = {
    val updatedCosts = partCosts map { pc =>
      PartCost findById pc.id.get match {
        case Some(partCost) => {
          PartCost modify (partCost.id.get, partCost)
          // this will return the option soon enough
          PartCost findById partCost.id.get
        }
        case _ => PartCost add pc
      }
    }

    if (updatedCosts contains (None)) error("Failed to update all part costs. Please Check")

    updatedCosts filter (_ isDefined) map (_ get)
  }*/
}
