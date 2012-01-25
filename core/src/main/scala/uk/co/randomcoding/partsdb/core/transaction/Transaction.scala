/**
 *
 */
package uk.co.randomcoding.partsdb.core.transaction

import uk.co.randomcoding.partsdb.core._
import id.{ Identifier, Identifiable }
import customer.Customer
import document.Document
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.field.ObjectIdRefListField
import net.liftweb.mongodb.record.field.ObjectIdRefField
import net.liftweb.mongodb.record.MongoMetaRecord

/**
 * Encapsulates all the data for a transaction between the company and a customer.
 *
 * @constructor Creates a new instance of a transaction.
 * @param transactionId The [[uk.co.randomcoding.partsdb.core.identifier.Identifier]] for this transaction.
 * @param customerId The [[uk.co.randomcoding.partsdb.core.identifier.Identifier]] of the [[uk.co.randomcoding.partsdb.core.customer.Customer]] that this transaction is for
 * @param documents An optional `Set` of [[uk.co.randomcoding.partsdb.core.identifier.Identifier]]s of all the [[uk.co.randomcoding.partsdb.core.document.Document]]s that were generated in this [[uk.co.randomcoding.partsdb.core.transaction.Transaction]]
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class Transaction extends MongoRecord[Transaction] with ObjectIdPk[Transaction] {
  def meta = Transaction

  object customer extends ObjectIdRefField(this, Customer)
  object documents extends ObjectIdRefListField(this, Document)
}

object Transaction extends Transaction with MongoMetaRecord[Transaction]

/*case class Transaction(transactionId: Identifier, customerId: Identifier, documents: Option[Set[Identifier]] = None) extends Identifiable {
  val identifierFieldName = "transactionId"
}*/