/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import scala.xml.Text
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.db.mongo.MongoUserAccess
import uk.co.randomcoding.partsdb.lift.util.CustomerDisplay
import uk.co.randomcoding.partsdb.lift.util.UserDisplay
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers.buttonLink
import uk.co.randomcoding.partsdb.lift.util.snippet.{ ErrorDisplay, DbAccessSnippet }
import net.liftweb.common.Logger
import net.liftweb.http.S
import net.liftweb.http.SHtml.link
import net.liftweb.util.Helpers._
import net.liftweb.common.Full
import uk.co.randomcoding.partsdb.core.id.Identifier
import uk.co.randomcoding.partsdb.lift.util.EntityDisplay
import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.lift.util.PartDisplay._
import uk.co.randomcoding.partsdb.lift.util.PartDisplay
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import uk.co.randomcoding.partsdb.lift.util.VehicleDisplay
/**
 * Displays the existing entities from the database.
 *
 * The entity type is specified by the `entityType` query parameter * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class DisplayExisting extends DbAccessSnippet with ErrorDisplay with Logger {

  /**
   * Display the web page content
   *
   * This sets the current title, details and add sections
   */
  def render = {
    val entityTypeParam = S.param("entityType")
    val entityTypeAttr = S.attr("entityType") openOr "Unspecified"

    val entityType = entityTypeParam match {
      case Full(e) => e
      case _ => entityTypeAttr
    }
    val highlightId = asLong(S.attr("highlight") openOr "No Highlight")
    val addText = "Add " + entityType
    val entitiesFromDb = matchingTypes(entityType)

    "#displayCurrentTitle" #> Text("%ss".format(entityType)) &
      "#details" #> displayTable(entitiesFromDb, entityType) &
      "#add" #> buttonLink("add%s".format(entityType), addText)
  }

  private[this] def displayTable(entities: List[AnyRef], entityType: String) = {
    entityType.toLowerCase match {
      case "customer" => CustomerDisplay.displayTable(entities map (_.asInstanceOf[Customer]))
      case "user" => UserDisplay.displayTable(entities map (_.asInstanceOf[(String, String)]))
      case "part" => PartDisplay.displayTable(entities map (_.asInstanceOf[Part]))
      case "vehicle" => VehicleDisplay.displayTable(entities map (_.asInstanceOf[Vehicle]))
      case _ => {
        error("Unknown Type: %s".format(entityType))
        EntityDisplay.emptyTable
      }
    }
  }

  private[this] lazy val matchingTypes: String => List[AnyRef] = (entityType: String) => {
    entityType.toLowerCase match {
      case "customer" => getAll[Customer]("customerId") sortBy (_.customerName)
      case "address" => getAll[Address]("addressId") sortBy (_.shortName)
      case "user" => MongoUserAccess().users sortBy (_._1)
      case "part" => getAll[Part]("partId") sortBy (_.partName)
      case "vehicle" => getAll[Vehicle]("vehicleId") sortBy (_.vehicleName)
      case "unspecified" => {
        error("Entity Type not specified.")
        List.empty
      }
      case _ => {
        error("Unhandled Entity Type: %s".format(entityType))
        List.empty
      }
    }
  }

  /**
   * Generates the table headings for the display of each type
   */
  /*private[this] def entitiesTableHeading(entityType: String) = {
    entityType.toLowerCase match {
      case "customer" => headings(customerHeadings)
      case "user" => headings(userHeadings)
      case _ => headings(Nil)
      case "customer" => headings("Customer Name", "Address", "Contact", "Payment Terms")
      case "part" => headings("Part Name", "Cost")
    }
  }*/

  /**
   * Convert a list of strings into a list to `<th>` elements.
   *
   * This also adds an extra column for the edit button that is put on the end of each row
   */
  /*private def headings(titles: List[String]) = (titles ::: "" :: Nil) map (title => <th>{ title }</th>)

  private[this] def displayEntity(entity: AnyRef) = {
    val (entityDetails, editLink) = entity match {
      case cust: Customer => (displayCustomer(cust), editEntityLink("Customer", cust.customerId))
      case (userName: String, userRole: String) => displayUser(userName, userRole)

  private[this] def displayEntities(entities: List[Identifiable]) = {
    /*entities match {
      case Nil => span(Text("Nothing to Display"), Noop)
      case _ =>*/ entities map (displayEntity _)
    //}
  }

  private[this] def displayEntity(entity: Identifiable) = {
    entity match {
      case cust: Customer => displayCustomer(cust)
      case part: Part => displayPart(part)

      case _ => {
        error("Unhandled entity type %s".format(entity.getClass.getSimpleName))
        val entityError = "Unable to display %s".format(entity)
        <div>{ entityError }</div>
      }
    }
    <tr valign="top">{ entityDetails }{ editLink }</tr>
  }

  private val editEntityLink = (entityType: String, entityId: Identifier) => <td>{ link("edit%s?id=%d".format(entityType, entityId.id), () => Unit, Text("Edit")) }</td>
  */
}
