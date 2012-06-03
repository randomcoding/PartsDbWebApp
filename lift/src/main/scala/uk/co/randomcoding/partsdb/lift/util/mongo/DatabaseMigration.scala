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

/**
 * Provides the capability to migrate the database between different versions.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object DatabaseMigration extends Logger {

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
  val versionMigrationFunctions: Map[Int, List[MigrationFunction]] = Map((1 -> migrateToVersion1Functions)).withDefaultValue(List.empty)

  /**
   * For migration to version 1 we essentially reset the database and ensure the default users are present.
   */
  private[this] val migrateToVersion1Functions = List(resetDatabase, addBootstrapUsers)

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
      val migrationResults = (currentVersion to newVersion) flatMap (ver => versionMigrationFunctions(ver) map (func => func()))

      migrationResults.toList.filter(_._2 == false) map (_._1) match {
        case Nil => {
          SystemData.databaseVersion = newVersion
          Nil
        }
        case errors => errors
      }
    }
    else {
      List("New version (%d) was less than current version (%d)".format(newVersion, currentVersion))
    }
  }

  /**
   * Reset all the tables in the database except the users table
   */
  private[this] val resetDatabase = () => {
    /*
     * The names of the collections in use by the database
     */
    val appCollections = Seq("addresss", "customers", "documentids", "documents", "partkits", "parts", "payments", "suppliers", "transactions", "users", "vehicles", "systemdatas")

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
    val resetSuccessful = resetCollections((appCollections filterNot (dontDrop contains _): _*))

    ("Reset of Database", resetSuccessful)
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
  private[this] val addBootstrapUsers = () => {
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

    val usersAdded = User.findUser("Dave").isDefined && User.findUser("Adam").isDefined

    ("Add Bootstrap Users", usersAdded)
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