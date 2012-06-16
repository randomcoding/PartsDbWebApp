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
package uk.co.randomcoding.partsdb.db.mongo

import com.foursquare.rogue.Rogue._
import com.mongodb.MongoException
import net.liftweb.common.Logger
import net.liftweb.mongodb.{ MongoDB, DefaultMongoIdentifier }
import scala.collection.JavaConversions._
import uk.co.randomcoding.partsdb.core.address.Address
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.document.{ Document, DocumentType }
import uk.co.randomcoding.partsdb.core.system.SystemData
import uk.co.randomcoding.partsdb.core.transaction.Transaction
import uk.co.randomcoding.partsdb.core.user.Role.{ USER, Role, NO_ROLE, ADMIN }
import uk.co.randomcoding.partsdb.db.util.Helpers._
import uk.co.randomcoding.partsdb.core.supplier.Supplier
import uk.co.randomcoding.partsdb.core.contact.ContactDetails
import uk.co.randomcoding.partsdb.core.document.LineItem
import org.bson.types.ObjectId

/**
 * Provides the capability to migrate the database between different versions.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object DatabaseMigration extends Logger {

  /**
   * The names of the collections in use by the database
   */
  val allUsedDbCollections = Seq("addresss", "customers", "documentids", "documents", "partkits", "parts", "payments", "suppliers", "transactions", "users", "vehicles", "systemdatas")

  private[this]type MigrationFunction = () => (String, Boolean)

  /**
   * This map is used to provide the incremental update functions that are required to
   * update the database from one version to the next.
   *
   * The mapping is from `Int` -> `List[() => Any]` where the `Int` key is the version of
   * the database to '''''migrate to''''' from the previous version.
   *
   * All version numbers are expected to be '''sequential''' as the version should only change when there
   * are changes that require migration.
   */
  lazy val versionMigrationFunctions: Map[Int, List[MigrationFunction]] = Map(1 -> migrateToVersion1Functions,
    2 -> migrateVersion1ToVersion2Functions,
    3 -> migrateVersion2ToVersion3Functions).withDefaultValue(List.empty)

  /**
   * For migration to version 1 we essentially reset the database and ensure the default users are present.
   */
  private[this] val migrateToVersion1Functions = List(() => ("Database Reset v1", resetCollections(allUsedDbCollections.filterNot(_ == "users"): _*)),
    () => ("Add Bootstrap Users", addBootstrapUsers))

  /**
   * V2 of the database ensures that there is a C.A.T.9 Supplier for Part Kits and 'services'
   */
  private[this] val migrateVersion1ToVersion2Functions = List(() => ("Ensure C.A.T.9 Supplier Exists", ensureCat9SupplierExists))

  /**
   * Version 3 of the database has Supplier information in the Line Items records so either all documents should be removed or
   * we update the line items with the Supplier's id derived from the part and its cost.
   */
  private[this] val migrateVersion2ToVersion3Functions = List(() => ("Ensure Line Items have correct Supplier Id fields", ensureLineItemsHaveASupplierField))

  /**
   * Perform migration to the specified version from the current version as identified by the
   * [[uk.co.randomcoding.partsdb.core.system.SystemData#databaseVersion]]
   *
   * @param newVersion The version to migrate to. If this is less than the current database version
   * then no migration functions will be executed.
   */
  def migrateToVersion(newVersion: Int): List[String] = {
    val currentVersion = SystemData.databaseVersion

    if (currentVersion < newVersion) {
      info("Migrating database from version %d to version %d".format(currentVersion, newVersion))
      val migrationResults = (currentVersion + 1 to newVersion) flatMap (ver => versionMigrationFunctions(ver) map (func => func()))

      migrationResults.toList.filter(_._2 == false) map (_._1) match {
        case Nil => {
          SystemData.databaseVersion = newVersion
          Nil
        }
        case errors => errors
      }
    }
    else {
      List("New version (%d) was less than or equal to the current version (%d)".format(newVersion, currentVersion))
    }
  }

  private[this] def resetCollections(collections: String*): Boolean = {
    MongoDB.getDb(DefaultMongoIdentifier) match {
      case Some(db) => {
        val dbCollections: Set[String] = db.getCollectionNames().toSet
        collections filter (dbCollections contains _) foreach (collection => {
          info("Dropping Collection: %s".format(collection))
          db.getCollection(collection).getFullName()
          db.getCollection(collection).drop()
          if (db.getCollectionNames() contains collection) error("Failed to drop collection: %s".format(collection))
        })
        val allCollectionsDropped = db.getCollectionNames() filter (collections.contains(_)) isEmpty

        allCollectionsDropped
      }
      case _ => {
        error("Unable to load Default Mongo Database!")
        false
      }
    }
  }

  /**
   *  Adds default users to add to the DB to bootstrap the login process
   *
   *  The added users are Dave (a User) and Adam (an Admin)
   *
   *  The users are not added if they already exist
   */
  private[this] def addBootstrapUsers(): Boolean = {
    import uk.co.randomcoding.partsdb.core.user.User
    try {
      if (User.findUser("Dave") isEmpty) User.addUser("Dave", hash("dave123"), USER)
      if (User.findUser("Adam") isEmpty) User.addUser("Adam", hash("adam123"), ADMIN)
    }
    catch {
      case e: MongoException => {
        if (e.getMessage startsWith "Collection not found") {
          User.createRecord.username("Adam").password(hash("adam123")).role(ADMIN).save
          User.createRecord.username("Dave").password(hash("dave123")).role(USER).save
        }
        else error("Exception whilst adding default users: %s".format(e.getMessage), e)
      }
    }

    User.findUser("Dave").isDefined && User.findUser("Adam").isDefined
  }

  private[this] def ensureCat9SupplierExists(): Boolean = {
    val cat9Name = "C.A.T.9 Limited"
    Supplier.where(_.supplierName eqs cat9Name).get match {
      case None => {
        val addr = Address.add("C.A.T.9 Business Address", "CAT 9\n2 Hackley Business Centre\nPencombe Lane\nBromyard\nHerefordshire\nHR74SP", "United Kingdom").get
        val contacts = ContactDetails.create("Nicky", "01885 488 663", "", "nicky.morris@cat-9.co.uk", "01885 488 663", true)
        Supplier.add(cat9Name, contacts, addr, Nil) isDefined
      }
      case Some(s) => true // do nothing
    }
  }

  private[this] def ensureLineItemsHaveASupplierField(): Boolean = {
    def docsToUpdate(): Seq[Document] = Document.or(_.where(_.lineItems.subfield(_.partSupplier) exists false),
      _.where(_.lineItems.subfield(_.partSupplier) eqs null)).fetch()

    docsToUpdate() foreach (doc => {
      val newLineItems = doc.lineItems.get map (lineItem => {
        lineItem.partSupplier.get match {
          case oid: ObjectId => lineItem
          case _ => supplierForLineItemPart(lineItem) match {
            case Some(oid) => lineItem.partSupplier(oid)
            case _ => lineItem // return unchanged
          }
        }
      })
      val newDocument = doc.lineItems(newLineItems)

      Document.update(doc.id.get, newDocument)
    })

    docsToUpdate().isEmpty
  }

  private[this] def supplierForLineItemPart(lineItem: LineItem): Option[ObjectId] = {
    val itemPartId = lineItem.partId.get
    val itemPrice = lineItem.basePrice.get
    Supplier.where(_.suppliedParts.subfield(_.part) eqs itemPartId).and(_.suppliedParts.subfield(_.suppliedCost) eqs itemPrice).get() match {
      case Some(s) => Some(s.id.get)
      case _ => None
    }
  }

  /*
   *  Deprecate functions from development cycles.
   *  
   *  Kept to demonstrate more complex transitions
   */
  @deprecated("No longer required as of 0.7.0", "0.7.0")
  private def setMissingOrderAndQuoteDocumentAddressesToCustomerAddressFromTransaction = {
    Transaction.fetch() foreach (transaction => customerAddressFromTransaction(transaction) match {
      case Some(customerAddress) => updateAddressOfOrdersAndQuotesWithNoAddress(transaction, customerAddress)
      case _ => {} // nothing
    })
  }

  @deprecated("No longer required as of 0.7.0", "0.7.0")
  private def customerAddressFromTransaction(transaction: Transaction): Option[Address] = Customer.where(_.id eqs transaction.customer.get).get match {
    case Some(cust) => Address.findById(cust.businessAddress.get)
    case _ => None
  }

  @deprecated("No longer required as of 0.7.0", "0.7.0")
  private def updateAddressOfOrdersAndQuotesWithNoAddress(transaction: Transaction, customerAddress: Address) = {
    Document.where(_.id in transaction.documents.get).
      and(_.documentType in Seq(DocumentType.Order, DocumentType.Quote)).
      and(_.documentAddress.subfield(_.addressText) eqs "").
      modify(_.documentAddress setTo customerAddress).updateMulti
  }
}