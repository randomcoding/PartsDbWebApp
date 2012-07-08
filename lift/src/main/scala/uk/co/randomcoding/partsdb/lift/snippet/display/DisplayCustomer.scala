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

import com.foursquare.rogue.Rogue._

import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.transaction.Transaction
import uk.co.randomcoding.partsdb.core.util.MongoHelpers._
import uk.co.randomcoding.partsdb.lift.util.TransformHelpers._
import uk.co.randomcoding.partsdb.lift.util.snippet._
import uk.co.randomcoding.partsdb.lift.util._

import net.liftweb.common.{ Logger, Full }
import net.liftweb.http.{ StatefulSnippet, S }
import net.liftweb.util.Helpers._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class DisplayCustomer extends StatefulSnippet with ErrorDisplay with AddressSnippet with ContactDetailsSnippet with TabDisplaySnippet with Logger {
  override val tabTitles = Seq(("quoteResults", "Quoted"), ("orderResults", "Ordered"), ("deliveryNoteResults", "Delivered"), ("invoiceResults", "Invoiced"), ("completedResults", "Completed"))

  private val cameFrom = S.referer openOr "/app/show?entityType=Customer"

  private val initialCustomer = S param "id" match {
    case Full(id) => Customer findById id
    case _ => None
  }

  private var (name, paymentTermsText) = initialCustomer match {
    case Some(cust) => (cust.customerName.get, "%d".format(cust.terms.get))
    case _ => ("", "30")
  }

  override var (addressText, addressCountry) = initialCustomer match {
    case Some(cust) => Address findById cust.businessAddress.get match {
      case Some(addr) => (addr.addressText.get, addr.country.get)
      case _ => ("", "United Kingdom")
    }
    case _ => ("", "United Kingdom")
  }

  override var (contactName, phoneNumber, mobileNumber, email, faxNumber) = initialCustomer match {
    case Some(cust) => cust.contactDetails.get match {
      case Nil => ("", "", "", "", "")
      case head :: tail => (head.contactName.get, head.phoneNumber.get, head.mobileNumber.get, head.emailAddress.get, head.faxNumber.get)
    }
    case _ => ("", "", "", "", "")
  }

  def customerId = initialCustomer match {
    case Some(cust) => cust.id.get.toString
    case _ => ""
  }

  private def transactions() = initialCustomer match {
    case Some(cust) => Transaction where (_.customer eqs cust.id.get) fetch
    case _ => List.empty
  }

  override def dispatch = {
    case "render" => render
  }

  def render = {
    val currentTransactions = transactions()
    "#formTitle" #> Text("Display Customer") &
      "#nameEntry" #> styledText(name, name = _, readonly) &
      renderReadOnlyAddress("Business Address", initialCustomer) &
      "#paymentTermsEntry" #> styledText(paymentTermsText, paymentTermsText = _, List(readonly, ("style", "width: 2em"))) &
      renderReadOnlyContactDetails() &
      "#recordPaymentButton" #> buttonLink("/app/recordPayment?customerId=%s".format(customerId), "Record Payment") &
      "#documentTabs" #> generateTabs() &
      "#quotes" #> TransactionSummaryDisplay(currentTransactions filter (_.transactionState == "Quoted")) &
      "#orders" #> TransactionSummaryDisplay(currentTransactions filter (_.transactionState == "Ordered")) &
      "#deliveryNotes" #> TransactionSummaryDisplay(currentTransactions filter (_.transactionState == "Delivered")) &
      "#invoices" #> TransactionSummaryDisplay(currentTransactions filter (_.transactionState == "Invoiced")) &
      "#completed" #> TransactionSummaryDisplay(currentTransactions filter (_.transactionState == "Completed"))
  }
}
