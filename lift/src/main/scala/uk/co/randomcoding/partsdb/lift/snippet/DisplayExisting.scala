/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import uk.co.randomcoding.partsdb.lift.util.snippet.ErrorDisplay
import uk.co.randomcoding.partsdb.lift.util.snippet.DbAccessSnippet
import uk.co.randomcoding.partsdb.lift.util.CustomerDisplay._
import net.liftweb.common.Logger
import uk.co.randomcoding.partsdb.core.id.Identifiable
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.address.Address
import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.util.Helpers._
import scala.xml.Text
import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.core.contact.Phone
import uk.co.randomcoding.partsdb.core.contact.Mobile
import uk.co.randomcoding.partsdb.core.contact.Email
import scala.xml.NodeSeq
import uk.co.randomcoding.partsdb.db.mongo.MongoUserAccess

/**
 * Displays the existing entities from the database.
 *
 * The entity type is specified by the `entityType` query parameter * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class DisplayExisting extends DbAccessSnippet with ErrorDisplay with Logger {

  def render = {
    val entityType = S.attr("entityType") openOr "Unspecified"
    val highlightId = asLong(S.attr("highlight") openOr "No Highlight")

    val entitiesFromDb = matchingTypes(entityType)

    "#displayCurrentTitle" #> Text("%ss".format(entityType)) &
      "#details" #> displayTable(entitiesFromDb, entityType)
  }

  private[this] def displayTable(entities: List[AnyRef], entityType: String) = {
    <table>
      <thead>
        <tr>{ entitiesTableHeading(entityType) }</tr>
      </thead>
      <tbody>
        { displayEntities(entities) }
      </tbody>
    </table>
  }

  private[this] lazy val matchingTypes: String => List[AnyRef] = (entityType: String) => {
    entityType.toLowerCase match {
      case "customer" => getAll[Customer]("customerId") sortBy (_.customerName)
      case "address" => getAll[Address]("addressId") sortBy (_.shortName)
      case "user" => MongoUserAccess().users sortBy (_._1)
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
  private[this] def entitiesTableHeading(entityType: String) = {
    entityType.toLowerCase match {
      case "customer" => headings("Customer Name", "Address", "Contact", "Payment Terms")
      case "user" => headings("User Name", "User Role")
    }
  }

  /**
   * Convert a list of strings into a list to `<th>` elements
   */
  private def headings(titles: String*) = titles map (title => <th>{ title }</th>)

  private[this] def displayEntities(entities: List[AnyRef]) = entities map (displayEntity _)

  private[this] def displayEntity(entity: AnyRef) = {
    entity match {
      case cust: Customer => displayCustomer(cust)
      case (userName: String, userRole: String) => <tr><td>{ userName }</td><td>{ userRole }</td></tr>
      case _ => {
        error("Unhandled entity type %s".format(entity.getClass.getSimpleName))
        val entityError = "Unable to display %s".format(entity)
        <div>{ entityError }</div>
      }
    }
  }
}