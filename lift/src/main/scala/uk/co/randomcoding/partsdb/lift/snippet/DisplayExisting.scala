/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import scala.xml.Text

import com.foursquare.rogue.Rogue._

import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.core.supplier.Supplier
import uk.co.randomcoding.partsdb.core.user.User
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet._
import uk.co.randomcoding.partsdb.lift.util._

import net.liftweb.common.{ Logger, Full }
import net.liftweb.http.S
import net.liftweb.util.Helpers._

/**
 * Displays the existing entities from the database.
 *
 * The entity type is specified by the `entityType` query parameter * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class DisplayExisting extends ErrorDisplay with Logger {

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

    "#displayCurrentTitle" #> Text("%ss".format(entityType)) &
      "#details" #> displayTable(entityType) &
      "#add" #> buttonLink("%s".format(entityType toLowerCase), addText)
  }

  private[this] def displayTable(entityType: String) = {
    entityType.toLowerCase match {
      case "customer" => CustomerDisplay(Customer where (_.id exists true) orderDesc (_.customerName) fetch)
      case "user" => UserDisplay(User where (_.id exists true) orderDesc (_.username) fetch)
      case "part" => PartDisplay(Part where (_.id exists true) orderDesc (_.partName) fetch)
      case "vehicle" => VehicleDisplay(Vehicle where (_.id exists true) orderDesc (_.vehicleName) fetch)
      case "supplier" => SupplierDisplay(Supplier where (_.id exists true) orderDesc (_.supplierName) fetch)
      //case "lineitem" => DisplayLineItem.displayTable(entities map (_.asInstanceOf[LineItem]))
      case _ => {
        error("Unknown Type: %s".format(entityType))
        TabularEntityDisplay.emptyTable
      }
    }
  }
}
