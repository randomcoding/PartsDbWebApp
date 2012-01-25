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
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class Transaction private () extends MongoRecord[Transaction] with ObjectIdPk[Transaction] {
  def meta = Transaction

  /**
   * The customer that this transaction is with
   */
  object customer extends ObjectIdRefField(this, Customer)

  /**
   * The documents that currently make up this transaction.
   */
  object documents extends ObjectIdRefListField(this, Document)
}

object Transaction extends Transaction with MongoMetaRecord[Transaction]

