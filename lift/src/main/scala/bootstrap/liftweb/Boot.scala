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
package bootstrap.liftweb

import net.liftweb.common.{ Full, Loggable }
import net.liftweb.http.{ LiftRules, Req, ResourceServer, GetRequest, Html5Properties, S, RedirectResponse }
import net.liftweb.sitemap.Loc
import net.liftweb.sitemap.Loc.{ If, Link, ExtLink, Hidden }
import net.liftweb.sitemap.Menu
import net.liftweb.sitemap.SiteMap
import net.liftweb.util.Props
import net.liftweb.util.Helpers.asInt
import uk.co.randomcoding.partsdb.core.user.Role._
import uk.co.randomcoding.partsdb.db.mongo.MongoConfig
import uk.co.randomcoding.partsdb.lift.model.Session
import uk.co.randomcoding.partsdb.lift.util.search.{ SearchProviders, CustomerSearchPageProvider }
import uk.co.randomcoding.partsdb.lift.util.mongo.DatabaseMigrationException
import uk.co.randomcoding.partsdb.db.mongo.DatabaseMigration
import uk.co.randomcoding.partsdb.core.system.SystemData
import net.liftweb.http.PermRedirectResponse
import net.liftweb.common.Empty
import net.liftweb.common.Box
import net.liftweb.sitemap.Loc.LocGroup

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */

class Boot extends Loggable {
  def boot {
    // Initialise MongoDB - MUST be run first
    MongoConfig.init(Props.get("mongo.db", "MainDb"))

    migrateDatabaseVersion

    configureAccessAndMenus

    configureLiftRules

    registerSearchProviders
  }

  @throws(classOf[DatabaseMigrationException])
  private[this] def migrateDatabaseVersion {
    val newDatabaseVersion = asInt(Props.get("current.db.version", "-1")) match {
      case Full(i) => i
      case _ => -1
    }

    // Perform any required database update operations
    DatabaseMigration.migrateToVersion(newDatabaseVersion) match {
      case Nil => logger.info("Successfully migrated to database version: %d".format(newDatabaseVersion))
      case errors => {
        val wrongVersionNumbersMessage = "New version (%d) was less than or equal to the current version (%d)".format(newDatabaseVersion, SystemData.databaseVersion)

        errors.find(_ == wrongVersionNumbersMessage) match {
          case Some(msg) => logger.warn(msg)
          case _ => logger.error("There were problems migrating the database")
        }
        val realErrors = errors.filterNot(_ == wrongVersionNumbersMessage)

        if (realErrors.nonEmpty) {
          realErrors foreach (logger.error(_))

          throw new DatabaseMigrationException("Failed Migrations: %s".format(errors.mkString("\n")))
        }
      }
    }
  }

  private[this] def configureAccessAndMenus {
    val userLoggedIn = If(() => Session.currentUser.get match {
      case (s: String, r: Role) => r == USER
      case _ => false
    }, () => RedirectResponse("/"))

    val adminLoggedIn = If(() => Session.currentUser.get match {
      case (s: String, r: Role) => r == ADMIN
      case _ => false
    }, () => RedirectResponse("/"))

    val loggedIn = If(() => Session.currentUser.get match {
      case (s, r) => r != NO_ROLE
      case _ => false
    }, () => RedirectResponse(Session.currentUser.get._2 match {
      case USER => "/app"
      case ADMIN => "/admin"
      case _ => "/"
    }))

    /*
     * Create the various menus here.
     *
     * To create a link to the show page use ExtLink to form the link with the '?entityType=...'
     * as the Link form incorrectly escapes the ? and = characters in the address bar.
     */
    val mainAppLoc = Menu(Loc("mainApp", new Link("app" :: Nil, true), "Home", userLoggedIn))

    // Create links for the show... parts here
    val showParts = Menu(Loc("showParts", ExtLink("/app/show?entityType=Part"), "Parts", userLoggedIn, Hidden, LocGroup("partsetc")))
    val showPartKits = Menu(Loc("showPartKits", ExtLink("/app/show?entityType=PartKit"), "Part Kits", userLoggedIn, Hidden, LocGroup("partsetc")))
    val showVehicles = Menu(Loc("showVehicles", ExtLink("/app/show?entityType=Vehicle"), "Vehicles", userLoggedIn, Hidden, LocGroup("partsetc")))
    val showSuppliers = Menu(Loc("showSuppliers", ExtLink("/app/show?entityType=Supplier"), "Suppliers", userLoggedIn, Hidden, LocGroup("partsetc")))

    // Search Button
    val searchLoc = Menu(Loc("search", new Link("app" :: "search" :: Nil, false), "Search", userLoggedIn))

    // Customers
    val showCustomers = Menu(Loc("showCustomers", ExtLink("/app/show?entityType=Customer"), "Customers", userLoggedIn, Hidden, LocGroup("customersetc")))
    // Add Quote Button
    val addQuoteLoc = Menu(Loc("addQuote", new Link("app" :: "quote" :: Nil, false), "New Quote", userLoggedIn, Hidden, LocGroup("customersetc")))

    // Payments Group
    val addPayment = Menu(Loc("recordPayment", new Link("app" :: "recordPayment" :: Nil, false), "Record Payment(s)", userLoggedIn, Hidden, LocGroup("payments")))
    val payInvoices = Menu(Loc("payInvoicesPayment", new Link("app" :: "payInvoices" :: Nil, false), "Pay Invoices", userLoggedIn, Hidden, LocGroup("payments")))

    // Main Menu Locs that are hidden - allows navigation
    val displayEntitiesLoc = Menu(Loc("displayEntities", new Link("app" :: "display" :: Nil, true), "Display Entities", Hidden, userLoggedIn))

    // Allow access to printing documents
    val printDocumentsLoc = Menu(Loc("printDocuments", new Link("app" :: "print" :: Nil, true), "Print Documents", Hidden, userLoggedIn))

    // Provide access to the admin menu. This is hidden.
    val adminLoc = Menu(Loc("adminSection", new Link("admin" :: Nil, true), "Admin", Hidden, adminLoggedIn))

    // The root of the app. Provides login
    val rootLoc = Menu(Loc("login", new Link("index" :: Nil, false), "Login", Hidden))

    // Construct the menu list to use - separated into displayed and hidden

    // The order of addition here is the order the menus are displayed in the navigation bar
    val displayedMenus = List(mainAppLoc, showParts, showPartKits, showVehicles, showSuppliers, showCustomers, addQuoteLoc, addPayment, payInvoices)
    val hiddenMenues = List(displayEntitiesLoc, printDocumentsLoc, adminLoc, rootLoc)

    val menus = displayedMenus ::: hiddenMenues
    LiftRules.setSiteMap(SiteMap(menus: _*))
  }

  private[this] def registerSearchProviders {
    // register search providers
    SearchProviders.register(CustomerSearchPageProvider)
    /*SearchProviders.register(QuoteSearchPageProvider)*/
  }

  private[this] def configureLiftRules {
    // packages to search for snippet code
    LiftRules.addToPackages("uk.co.randomcoding.partsdb.lift")

    // Use jQuery 1.4
    LiftRules.jsArtifacts = net.liftweb.http.js.jquery.JQuery14Artifacts

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

    LiftRules.dispatch.append {
      case Req("logout" :: Nil, _, GetRequest) =>
        S.request.foreach(_.request.session.terminate) //
        S.redirectTo("/")
    }

    ResourceServer.allow {
      case "css" :: _ => true
      case "js" :: _ => true
    }
  }
}
