/*
 * Copyright (C) 2012 RandomCoder <randomcoder@randomcoding.co.uk>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *    RandomCoder - initial API and implementation and/or initial documentation
 */

/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet.display

import scala.xml.Text

import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.document.DocumentType.{ Quote, Order, Invoice, DocType, DeliveryNote }
import uk.co.randomcoding.partsdb.core.document.Document
import uk.co.randomcoding.partsdb.core.transaction.Transaction
import uk.co.randomcoding.partsdb.core.util.MongoHelpers._
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet.display.{ QuoteDetailDisplay, OrderDetailDisplay, InvoiceDetailDisplay, DeliveryNoteDetailDisplay }
import uk.co.randomcoding.partsdb.lift.util.snippet._

import net.liftweb.common.{ Logger, Full }
import net.liftweb.http.S
import net.liftweb.util.Helpers._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object DisplayTransaction extends TabDisplaySnippet with Logger {

  override val tabTitles = Seq(("quoteResults", "Quoted"), ("orderResults", "Ordered"), ("deliveryNoteResults", "Delivered"), ("invoiceResults", "Invoiced"))

  /*
   * Form a closure over passed in data to enable the rendering of the correct info
   */
  def render = {
    val cameFrom = S.referer openOr "/app/show?entityType=Customer"

    var currentDocumentType: Option[DocType] = None

    val transaction = S.param("id") match {
      case Full(id) => Transaction.findById(id)
      case _ => None
    }

    def generateDocumentDisplays() = transaction match {
      case None => "#documentTabs" #> generateTabs()
      case Some(t) => {
        val documents = t.documents.get map (Document.findById(_)) filter (_ isDefined) map (_.get)
        debug("Found %s documents".format(documents.mkString(", ")))
        val transactionId = t.id.get.toString
        "#documentTabs" #> generateTabs() &
          "#quotes *" #> QuoteDetailDisplay(documents filter (_.documentType.get == Quote), transactionId) &
          "#orders *" #> OrderDetailDisplay(documents filter (_.documentType.get == Order), transactionId) &
          "#deliveryNotes" #> DeliveryNoteDetailDisplay(documents filter (_.documentType.get == DeliveryNote), transactionId) &
          "#invoices *" #> InvoiceDetailDisplay(documents filter (_.documentType.get == Invoice), transactionId)
      }
    }

    /*
     * Perform actual render of page
     */

    debug("Rendering details for transaction: %s".format(transaction))
    val transactionTitleText = transaction match {
      case Some(t) => t.shortName
      case _ => "No Transaction"
    }

    val customerNameText = transaction match {
      case Some(t) => Customer.findById(t.customer.get) match {
        case Some(c) => c.customerName.get
        case _ => "No Customer"
      }
      case _ => "No Transaction"
    }

    "#formTitle" #> Text(transactionTitleText) &
      "#backLink" #> buttonLink(" <- Back", cameFrom) &
      "#customerName" #> Text(customerNameText) &
      generateDocumentDisplays()
  }
}
