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
package uk.co.randomcoding.partsdb.lift.util

import scala.io.Source
import scala.xml.NodeSeq.seqToNodeSeq
import scala.xml.NodeSeq

import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.contact.ContactDetails

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object SnippetDisplayHelpers {

  def displayAddressCell(address: Address): NodeSeq = {
    val addressLines = Source.fromString(address.addressText.get).getLines()
    <span>{ addressLines map (line => <span>{ line }</span><br/>) }</span> ++
      <span>{ address.country.get }</span>
  }

  def displayContactCell(contactDetails: ContactDetails): NodeSeq = {
    <span>{ contactDetails.contactName.get }</span><br/>
    ++ numbersDetails (contactDetails)
  }

  private[this] def numbersDetails(contactDetails: ContactDetails): NodeSeq = {
    val details = (detailString: String, heading: String) =>
      detailString.trim match {
        case "" => Nil
        case other => <span>{ "%s: %s".format(heading, detailString) }</span><br/>
      }

    details(contactDetails.phoneNumber.get, "Phone") ++
      details(contactDetails.mobileNumber.get, "Mobile") ++
      details(contactDetails.faxNumber.get, "Fax") ++
      details(contactDetails.emailAddress.get, "EMail")
  }

  def currencyFormat(value: Double): String = "£%02.2f".format(value)
}
