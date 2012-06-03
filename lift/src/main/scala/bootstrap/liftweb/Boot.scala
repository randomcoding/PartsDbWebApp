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
import uk.co.randomcoding.partsdb.lift.util.mongo.DatabaseMigration
import uk.co.randomcoding.partsdb.lift.util.search.{ SearchProviders, CustomerSearchPageProvider }

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */

class Boot extends Loggable {
  def boot {
    // Initialise MongoDB - MUST be run first
    MongoConfig.init(Props.get("mongo.db", "MainDb"))

    // Perform any required database update operations
    DatabaseMigration.migrateToVersion(asInt(Props.get("current.db.version", "-1")) match {
      case Full(i) => i
      case _ => -1
    })

    configureAccessAndMenus

    configureLiftRules

    registerSearchProviders
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
    val showCustomers = Menu(Loc("showCustomers", ExtLink("/app/show?entityType=Customer"), "Customers", userLoggedIn))
    val showParts = Menu(Loc("showParts", ExtLink("/app/show?entityType=Part"), "Parts", userLoggedIn))
    val showSuppliers = Menu(Loc("showSuppliers", ExtLink("/app/show?entityType=Supplier"), "Suppliers", userLoggedIn))
    val showVehicles = Menu(Loc("showVehicles", ExtLink("/app/show?entityType=Vehicle"), "Vehicles", userLoggedIn))
    val showPartKits = Menu(Loc("showPartKits", ExtLink("/app/show?entityType=PartKit"), "Part Kits", userLoggedIn))

    // Search Button
    val searchLoc = Menu(Loc("search", new Link("app" :: "search" :: Nil, false), "Search", userLoggedIn))

    // Add Quote Button
    val addQuoteLoc = Menu(Loc("addQuote", new Link("app" :: "quote" :: Nil, false), "New Quote", userLoggedIn))

    // Payments Menu

    // Payments Button
    val addPayment = Menu(Loc("recordPayment", new Link("app" :: "recordPayment" :: Nil, false), "Record Payment(s)", userLoggedIn))
    val payInvoices = Menu(Loc("payInvoicesPayment", new Link("app" :: "payInvoices" :: Nil, false), "Pay Invoices", userLoggedIn))

    // Display... locs hidden
    val displayEntitiesLoc = Menu(Loc("displayEntities", new Link("app" :: "display" :: Nil, true), "Display Entities", Hidden, userLoggedIn))

    // Allow access to printing documents
    val printDocumentsLoc = Menu(Loc("printDocuments", new Link("app" :: "print" :: Nil, true), "Print Documents", Hidden, userLoggedIn))

    // Provide access to the admin menu. This is hidden.
    val adminLoc = Menu(Loc("adminSection", new Link("admin" :: Nil, true), "Admin", Hidden, adminLoggedIn))

    // The root of the app. Provides login
    val rootLoc = Menu(Loc("login", new Link("index" :: Nil, false), "Login", Hidden))

    // Construct the menu list to use - separated into displayed and hidden

    // The order of addition here is the order the menus are displayed in the navigation bar
    val displayedMenus = List(mainAppLoc, showCustomers, showParts, showPartKits, showVehicles, showSuppliers, searchLoc, addQuoteLoc, addPayment, payInvoices)
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
