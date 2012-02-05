/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import scala.xml.Text
import org.bson.types.ObjectId
import com.foursquare.rogue.Rogue._
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.core.part.{ PartCost, Part }
import uk.co.randomcoding.partsdb.core.supplier.Supplier
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet._
import net.liftweb.common.StringOrNodeSeq.strTo
import net.liftweb.common.{ Logger, Full }
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmds.{ SetHtml, Noop }
import net.liftweb.http.js.JsCmd
import net.liftweb.http.{ StatefulSnippet, S }
import net.liftweb.util.Helpers._
import org.joda.time.DateTime
import uk.co.randomcoding.partsdb.lift.util.PartCostDisplay

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddEditSupplier extends StatefulSnippet with AddressSnippet with ContactDetailsSnippet with DataValidation with ErrorDisplay with Logger {

  val cameFrom = S.referer openOr "app/show?entityType=Supplier"
  /*
   * Have we been called with an id= param that is the id of a Supplier?
   */
  val initialSupplier = S param ("id") match {
    case Full(id) => Supplier findById new ObjectId(id)
    case _ => None
  }

  /*
   * Set the address fields of the address snippet based on the initial supplier
   */
  override var (addressText, addressCountry) = initialSupplier match {
    case Some(s) => Address findById (s.businessAddress.get) match {
      case Some(addr) => (addr.addressText.get, addr.country.get)
      case _ => ("", "")
    }
    case _ => ("", "")
  }

  /*
   * Set the contact details fields of the contact details snippet based on the initial supplier
   */
  override var (contactName, phoneNumber, mobileNumber, email) = initialSupplier match {
    case Some(s) => ContactDetails findById s.contactDetails.get match {
      case Some(c) => (c.contactName.get, c.phoneNumber.get, c.mobileNumber.get, c.emailAddress.get)
      case _ => ("", "", "", "")
    }
    case _ => ("", "", "", "")
  }

  /*
   * Set the supplier name based on the initial supplier
   */
  var (supplierName, currentPartCosts) = initialSupplier match {
    case Some(s) => (s.supplierName.get, s.suppliedParts.get map (PartCost findById _) filter (_ isDefined) map (_ get))
    case _ => ("", Nil)
  }

  def dispatch = {
    case "render" => render
  }

  val parts = Part where (_.id exists true) orderDesc (_.partName) fetch
  val partsSelect = (None, "Select Part") :: (parts map ((p: Part) => (Some(p), p.partName.get)))

  val defaultDate = new DateTime(1970, 1, 1, 12, 00)

  var currentPart: Option[Part] = None
  var currentPartCost = 0.0d
  var currentPartLastSuppliedDate: DateTime = defaultDate
  val dateFormat = "dd/MM/yyyy"
  def dateString(date: DateTime) = date.toString("dd/MM/yyyy")

  def render = {

    "#formTitle" #> Text("Add Supplier") &
      "#nameEntry" #> styledText(supplierName, supplierName = _) &
      renderAddress() &
      renderContactDetails() &
      "#partSelect" #> styledAjaxObjectSelect(partsSelect, currentPart, updateAjaxValue[Option[Part]](currentPart = _)) &
      "#costEntry" #> styledAjaxText("%.2f".format(currentPartCost), updateAjaxValue[String](cost => currentPartCost = asDouble(cost) match {
        case Full(d) => d
        case _ => 0.0d
      })) &
      "#lastSuppliedDate" #> styledAjaxText(dateString(currentPartLastSuppliedDate), updateAjaxValue[String](date => currentPartLastSuppliedDate = {
        val dateParts = date split ("/") map (asInt(_) openOr -1)
        if (dateParts contains -1) {
          displayError("Error Location", "Cannot create Date from: %s".format(date))
          defaultDate
        }
        else {
          new DateTime(dateParts(0), dateParts(1), dateParts(2))
        }
      })) &
      "#addPartCost" #> styledAjaxButton("Add Part Cost", addPartCost)
    "#submit" #> button("Submit", processSubmit)
  }

  private def addPartCost(): JsCmd = {
    clearErrors
    (currentPart, currentPartCost, currentPartLastSuppliedDate) match {
      case (_, cost: Double, _) if (cost <= 0.0d) => // cost error
      case (_, _, date: DateTime) if (date equals defaultDate) => // date error
      case (None, _, _) => // Part select error
      case (Some(p), cost: Double, date: DateTime) if (cost > 0.0 && date != defaultDate) => // all ok
    }

    refreshPartCostDisplay()
  }

  private[this] def refreshPartCostDisplay(): JsCmd = {
    SetHtml("currentPartCosts", PartCostDisplay.displayTable(currentPartCosts))
  }

  private[this] def processSubmit(): JsCmd = {
    val address = addressFromInput(supplierName)
    val contacts = contactDetailsFromInput

    val validationItems = Seq(
      ValidationItem(address, "addressErrorId", "Address Entry was invalid"),
      ValidationItem(contacts, "contactDetailsId", "Contact Details entry was invalid"))

    validate(validationItems: _*) match {
      case Nil => {
        initialSupplier match {
          case Some(s) => modifySupplier(s, supplierName, contacts, address.get)
          case _ => addSupplier(supplierName, contacts, address.get)
        }
      }
      case errors => displayError(errors: _*)
    }

    Noop
  }

  private[this] def addSupplier(name: String, contacts: ContactDetails, address: Address): JsCmd = {

    val contact = updateContactDetails(contacts)

    val addr = updateAddress(address)

    Supplier.add(name, contact.get, addr.get, Nil) match {
      case Some(s) => S redirectTo cameFrom
      case _ => Noop
    }
  }

  private[this] def modifySupplier(supplier: Supplier, newName: String, newContacts: ContactDetails, newAddress: Address): JsCmd = {
    val contacts = updateContactDetails(newContacts)

    val addr = updateAddress(newAddress)

    val suppliedParts = supplier.suppliedParts.get map (PartCost findById _) filter (_ isDefined) map (_ get)
    Supplier.modify(supplier.id.get, newName, contacts.get, addr.get, suppliedParts, supplier.notes.get)

    S redirectTo cameFrom
  }
}