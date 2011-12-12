/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import scala.xml.Text
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.db.mongo.MongoUserAccess
import uk.co.randomcoding.partsdb.lift.util.CustomerDisplay._
import uk.co.randomcoding.partsdb.lift.util.UserDisplay._
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers.buttonLink
import uk.co.randomcoding.partsdb.lift.util.snippet.{ ErrorDisplay, DbAccessSnippet }
import net.liftweb.common.Logger
import net.liftweb.http.S
import net.liftweb.util.Helpers._
import net.liftweb.common.Full

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
    <table>
      <thead>
        <tr>{ entitiesTableHeading(entityType) }</tr>
      </thead>
      <tbody>
        { entities map (displayEntity _) }
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
      case "customer" => headings(customerHeadings)
      case "user" => headings(userHeadings)
      case _ => headings(Nil)
    }
  }

  /**
   * Convert a list of strings into a list to `<th>` elements
   */
  private def headings(titles: Seq[String]) = titles map (title => <th>{ title }</th>)

  //private[this] def displayEntities(entities: List[AnyRef]) = entities map (displayEntity _)

  private[this] def displayEntity(entity: AnyRef) = {
    entity match {
      case cust: Customer => displayCustomer(cust)
      case (userName: String, userRole: String) => displayUser(userName, userRole)
      case _ => {
        error("Unhandled entity type %s".format(entity.getClass.getSimpleName))
        val entityError = "Unable to display %s".format(entity)
        <div>{ entityError }</div>
      }
    }
  }
}