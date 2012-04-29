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
package uk.co.randomcoding.partsdb.lift.util.mongo

import net.liftweb.mongodb.MongoDB
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.transaction.Transaction
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.document.Document
import net.liftweb.mongodb.DefaultMongoIdentifier
import net.liftweb.common.Logger
import net.liftweb.common.Loggable
import com.foursquare.rogue.Rogue._
import scala.collection.JavaConversions._
import uk.co.randomcoding.partsdb.core.document.DocumentType

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object DatabaseCleanupOperations extends Loggable {

  /**
   * Run any required cleanup operations on the database
   */
  final def cleanUpDatabase() = {
    // Uncomment this to add new users required for user access initialisation
    //addBootstrapUsers

    // Uncomment this to reset all the data in the database except the user data.
    // This can be modified in the resetDatabase method
    // resetDatabase

    // Uncomment this to update the document address field of all Orders and Quotes that have an empty document address
    // to the same as the customer's address from the transaction that contains the document.
    setMissingOrderAndQuoteDocumentAddressesToCustomerAddressFromTransaction
  }

  private def resetDatabase {
    /*
     * The names of the collections in use by the database
     */
    val appCollections = Seq("addresss", "contactdetailss", "customers", "documentids", "documents", "parts", "suppliers", "transactions", "users", "vehicles")

    /*
     * Name of collections not to drop.
     * 
     * By default, don't drop the user data as this is not related to the main running of the app and will deny people access.
     */
    val dontDrop = Seq("users")

    /* 
     * Drops all data from the named collections
     * 
     * If you want to exclude any collection from being dropped simply add its name to dontDrop above
     * 
     * Available collections are shown in the appCollections Seq
     */
    resetCollections((appCollections filterNot (dontDrop contains _): _*))
  }

  private def resetCollections(collections: String*) = {
    MongoDB.getDb(DefaultMongoIdentifier) match {
      case Some(db) => {
        val dbCollections: Set[String] = db.getCollectionNames().toSet
        collections filter (dbCollections contains _) foreach (collection => {
          logger.info("Dropping Collection: %s".format(collection))
          db.getCollection(collection).getFullName()
          db.getCollection(collection).drop()
          if (db.getCollectionNames() contains collection) logger.error("Failed to drop collection: %s".format(collection))
        })
      }
      case _ => logger.error("Unable to load Default Mongo Database!")
    }
  }

  private def setMissingOrderAndQuoteDocumentAddressesToCustomerAddressFromTransaction = {
    Transaction.fetch() foreach (transaction => customerAddressFromTransaction(transaction) match {
      case Some(customerAddress) => updateAddressOfOrdersAndQuotesWithNoAddress(transaction, customerAddress)
      case _ => {} // nothing
    })
  }

  private def customerAddressFromTransaction(transaction: Transaction): Option[Address] = Customer.where(_.id eqs transaction.customer.get).get match {
    case Some(cust) => Address.findById(cust.businessAddress.get)
    case _ => None
  }

  private def updateAddressOfOrdersAndQuotesWithNoAddress(transaction: Transaction, customerAddress: Address) = {
    Document.where(_.id in transaction.documents.get).
      and(_.documentType in Seq(DocumentType.Order, DocumentType.Quote)).
      and(_.documentAddress.subfield(_.addressText) eqs "").
      modify(_.documentAddress setTo customerAddress).updateMulti
  }
}