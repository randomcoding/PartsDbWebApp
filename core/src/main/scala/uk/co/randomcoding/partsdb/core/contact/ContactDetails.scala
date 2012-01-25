/**
 *
 */
package uk.co.randomcoding.partsdb.core.contact

import net.liftweb.mongodb.record.field._
import net.liftweb.mongodb.record.{ MongoRecord, MongoMetaRecord }
import net.liftweb.record.field._

/**
 * Some contact details for a customer or supplier.
 *
 * This associates a Contact's name to
 *
 * @constructor Create a new Contact details instance.
 *
 * @param contacts A map of contact name -> contact number(s)
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
case class ContactDetails(val contactName: String, val phoneNumbers: Option[List[Phone]] = None, val mobileNumbers: Option[List[Mobile]] = None, val emailAddresses: Option[List[Email]] = None)

object NullContactDetails extends ContactDetails("")