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
package uk.co.randomcoding.partsdb.core.contact

import net.liftweb.mongodb.record._
import net.liftweb.record.field._

/**
 * Some contact details for a customer or supplier.
 *
 * Associates a phone number, mobile number and email to a name
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
class ContactDetails private () extends BsonRecord[ContactDetails] {

  def meta = ContactDetails

  object contactName extends StringField(this, 50)

  object phoneNumber extends StringField(this, 50)

  object mobileNumber extends StringField(this, 50)

  object emailAddress extends StringField(this, 50)

  object faxNumber extends StringField(this, 50)

  object isPrimary extends BooleanField(this)

  override def equals(that: Any): Boolean = that match {
    case other: ContactDetails => {
      contactName.get == other.contactName.get &&
        phoneNumber.get == other.phoneNumber.get &&
        mobileNumber.get == other.mobileNumber.get &&
        emailAddress.get == other.emailAddress.get &&
        faxNumber.get == other.faxNumber.get
    }
    case _ => false
  }

  private val hashCodeFields = List(contactName, phoneNumber, mobileNumber, emailAddress, faxNumber)

  override def hashCode: Int = getClass.hashCode + (hashCodeFields map (_.get.hashCode) sum)

  def matches(other: ContactDetails): Boolean = {
    if (contactName.get == other.contactName.get) {
      phoneNumber.get == other.phoneNumber.get ||
        mobileNumber.get == other.mobileNumber.get ||
        emailAddress.get == other.emailAddress.get ||
        faxNumber.get == other.faxNumber.get
    }
    else false
  }
}

object ContactDetails extends ContactDetails with BsonMetaRecord[ContactDetails] {

  /**
   * Create a new `ContactDetails` record
   */
  def apply(contactName: String, phoneNumber: String, mobileNumber: String, emailAddress: String, faxNumber: String, isPrimary: Boolean): ContactDetails = {
    create(contactName, phoneNumber, mobileNumber, emailAddress, faxNumber, isPrimary)
  }

  /**
   * Create a new `ContactDetails` record
   */
  def create(contactName: String, phoneNumber: String, mobileNumber: String, emailAddress: String, faxNumber: String, isPrimary: Boolean): ContactDetails = {
    ContactDetails.createRecord.contactName(contactName).phoneNumber(phoneNumber).mobileNumber(mobileNumber).emailAddress(emailAddress).faxNumber(faxNumber).isPrimary(isPrimary)
  }

}
