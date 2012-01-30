/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util

import uk.co.randomcoding.partsdb.core.contact.Mobile
import uk.co.randomcoding.partsdb.core.contact.Email
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmds.Noop
import uk.co.randomcoding.partsdb.core.contact.Phone
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import scala.xml.NodeSeq
import scala.xml.Text
import net.liftweb.common.Logger
import scala.io.Source
import uk.co.randomcoding.partsdb.db.DbAccess
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.document.Document

/**
 * Helper functions for displaying customers in lift pages
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object QuoteDisplay extends EntityDisplay with Logger {
  type EntityType = Document
  /**
   * The headings to use for the display of the customer data table
   */
  override val rowHeadings = List("Quote Number")

  /**
   * Generates html to display a customer.
   *
   * Currently displays the name, terms and contact details
   *
   * @param customer The [[uk.co.randomcoding.partsdb.core.customer.Customer]] to display
   * @return A [[scala.xml.NodeSeq]] of `<td>` elements to display the customer details
   */
  override def displayEntity(doc: Document): NodeSeq = {
    <td>{ doc.documentNumber }</td> ++ <td></td>
    //editEntityCell(editEntityLink("Quote", doc.id))
  }
}