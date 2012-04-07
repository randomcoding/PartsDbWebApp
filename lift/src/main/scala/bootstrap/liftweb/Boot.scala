package bootstrap.liftweb

import com.mongodb.MongoException
import uk.co.randomcoding.partsdb.core.user.Role.{ USER, Role, NO_ROLE, ADMIN }
import uk.co.randomcoding.partsdb.core.user.User.addUser
import uk.co.randomcoding.partsdb.db.mongo.MongoConfig
import uk.co.randomcoding.partsdb.db.util.Helpers._
import uk.co.randomcoding.partsdb.lift.model.Session
import uk.co.randomcoding.partsdb.lift.util.search._
import net.liftweb.common.{ Loggable, Full }
import net.liftweb.http.LiftRulesMocker.toLiftRules
import net.liftweb.http.{ S, ResourceServer, Req, RedirectResponse, LiftRules, Html5Properties, GetRequest }
import net.liftweb.sitemap.Loc.LinkText.strToLinkText
import net.liftweb.sitemap.Loc.{ Link, If, Hidden, ExtLink }
import net.liftweb.sitemap.{ SiteMap, Menu, Loc }
import net.liftweb.util.Vendor.valToVender
import net.liftweb.util.Props
import com.mongodb.Mongo
import net.liftweb.mongodb.MongoDB
import net.liftweb.mongodb.DefaultMongoIdentifier
import scala.collection.JavaConversions._

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
     * The names of the collections in use by the database
     */
    val appCollections = Seq("addresss", "contactdetailss", "customers", "documentids", "documents", "parts", "suppliers", "transactions", "users", "vehicles")

    /*
     * Name of collections not to drop during a call to resetCollections(String*)
     */
    val dontDrop = Seq("users")

    /* 
     * Uncomment this line to drop all data from the named collections
     * 
     * If you want to exclude any collection from being dropped simply add its name to the dontDtop Seq
     * 
     * Available collections are shown in the appCollections Seq
     */
    //resetCollections((appCollections filterNot (dontDrop contains _): _*))

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

    // Search Button
    val searchLoc = Menu(Loc("search", new Link("app" :: "search" :: Nil, false), "Search", userLoggedIn))

    // Add Quote Button
    val addQuoteLoc = Menu(Loc("addQuote", new Link("app" :: "quote" :: Nil, false), "New Quote", userLoggedIn))

    // Add Payment Button
    val addPayment = Menu(Loc("recordPayment", new Link("app" :: "recordPayment" :: Nil, false), "Record Payment", userLoggedIn))

    // Display... locs hidden
    val displayEntitiesLoc = Menu(Loc("displayEntities", new Link("app" :: "display" :: Nil, true), "Display Entities", Hidden, userLoggedIn))

    // Provide access to the admin menu. This is hidden.
    val adminLoc = Menu(Loc("adminSection", new Link("admin" :: Nil, true), "Admin", Hidden, adminLoggedIn))

    // The root of the app. Provides login
    val rootLoc = Menu(Loc("login", new Link("index" :: Nil, false), "Login", Hidden))

    // Construct the menu list to use
    val menus = mainAppLoc :: showCustomers :: showParts :: showVehicles :: showSuppliers :: searchLoc :: addQuoteLoc :: addPayment :: displayEntitiesLoc :: adminLoc :: rootLoc :: Nil

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
    SearchProviders.register(CustomerSearchPageProvider)
    /*SearchProviders.register(QuoteSearchPageProvider)*/
  }

  // Default users to add to the DB to bootstrap the login process
  private[this] def addBootstrapUsers: Unit = {
    import uk.co.randomcoding.partsdb.core.user.User
    try {
      User.addUser("Dave", hash("dave123"), USER)
      User.addUser("Adam", hash("adam123"), ADMIN)
    }
    catch {
      case e: MongoException => {
        if (e.getMessage startsWith "Collection not found") {
          User.createRecord.username("Adam").password(hash("adam123")).role(ADMIN).save
          //User.createRecord.username("Dave").password(hash("dave123")).role(USER).save
        }
      }
    }
  }

  private[this] def resetCollections(collections: String*) = {
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
}
