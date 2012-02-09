package bootstrap.liftweb

import uk.co.randomcoding.partsdb.lift.util.auth.AppAuthentication
import uk.co.randomcoding.partsdb.lift.model.Session
import net.liftweb.common.{ Loggable, Full }
import net.liftweb.http._
import net.liftweb.http.auth.AuthRole
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import uk.co.randomcoding.partsdb.lift.util.search.SearchProviders
import uk.co.randomcoding.partsdb.db.mongo.MongoConfig
import net.liftweb.util.Props
import uk.co.randomcoding.partsdb.core.user.Role._
import uk.co.randomcoding.partsdb.core.user.User
import uk.co.randomcoding.partsdb.db.util.Helpers._

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot extends Loggable {
  def boot {
    // Initialise MongoDB
    MongoConfig.init(Props.get("mongo.db", "MainDb"))

    // packages to search for snippet code
    LiftRules.addToPackages("uk.co.randomcoding.partsdb.lift")

    // Uncomment this to add new users required for user access initialisation
    //addBootstrapUsers

    /*
     * Create the various menus here.
     * 
     * To create a link to the show page use ExtLink to form the link with the '?entityType=...'
     * as the Link form incorrectly escapes the ? and = characters in the address bar.
     */
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

    val mainAppLoc = Menu(Loc("mainApp", new Link("app" :: Nil, true), "Home", userLoggedIn))

    // Create links for the show... parts here
    val showCustomers = Menu(Loc("showCustomers", ExtLink("/app/show?entityType=Customer"), "Customers", userLoggedIn))
    val showParts = Menu(Loc("showParts", ExtLink("/app/show?entityType=Part"), "Parts", userLoggedIn))
    val showSuppliers = Menu(Loc("showSuppliers", ExtLink("/app/show?entityType=Supplier"), "Suppliers", userLoggedIn))
    val showVehicles = Menu(Loc("showVehicles", ExtLink("/app/show?entityType=Vehicle"), "Vehicles", userLoggedIn))

    // Search Button
    val searchLoc = Menu(Loc("search", new Link("app" :: "search" :: Nil, false), "Search", userLoggedIn))

    // Add Quote Button
    val addQuoteLoc = Menu(Loc("addQuote", new Link("app" :: "quote" :: Nil, false), "New Quote", userLoggedIn))

    // Provide access to the admin menu. This is hidden.
    val adminLoc = Menu(Loc("adminSection", new Link("admin" :: Nil, true), "Admin", Hidden, adminLoggedIn))

    // The root of the app. Provides login
    val rootLoc = Menu(Loc("login", new Link("index" :: Nil, false), "Login", Hidden))

    // Construct the menu list to use
    val menus = mainAppLoc :: showCustomers :: showParts :: showVehicles :: showSuppliers :: searchLoc :: addQuoteLoc :: adminLoc :: rootLoc :: Nil

    LiftRules.setSiteMap(SiteMap(menus: _*))

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

    // register search providers
    /*SearchProviders.register(CustomerSearchPageProvider)
    SearchProviders.register(QuoteSearchPageProvider)*/
  }

  // Default users to add to the DB to bootstrap the login process
  private[this] def addBootstrapUsers: Unit = {
    import uk.co.randomcoding.partsdb.core.user.User._
    addUser("Dave", hash("dave123"), USER)
    addUser("Adam", hash("adam123"), ADMIN)
  }
}
