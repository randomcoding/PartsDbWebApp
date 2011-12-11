package bootstrap.liftweb

import uk.co.randomcoding.partsdb.lift.util.auth.AppAuthentication

import net.liftweb.common.{ Loggable, Full }
import net.liftweb.http._
import net.liftweb.http.auth.AuthRole
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot extends Loggable {
  def boot {
    // where to search for snippet code
    LiftRules.addToPackages("uk.co.randomcoding.partsdb.lift")

    // authentication
    LiftRules.httpAuthProtectedResource.prepend {
      case (Req("admin" :: _, _, _)) => Full(AuthRole("Admin"))
      case (Req("app" :: _, _, _)) => Full(AuthRole("User"))
    }

    LiftRules.authentication = AppAuthentication.simpleAuth

    /*
     * This provides access control to pages. 
     * In order to allow a page, add an entry here
     */
    def sitemap = SiteMap(
      Menu.i("Home") / "app" / "index",
      Menu.i("Customers") / "app" / "customers",
      Menu.i("Parts") / "app" / "parts",
      Menu.i("Suppliers") / "app" / "suppliers",
      // hidden entries
      Menu.i("Add Customer") / "app" / "addCustomer" >> Hidden,
      Menu.i("Add Part") / "app" / "addPart" >> Hidden,
      Menu.i("Add Supplier") / "app" / "addSupplier" >> Hidden,
      // Admin Section
      Menu.i("Admin") / "admin" / "index" >> Hidden,
      Menu.i("Admin Add User") / "admin" / "addUser" >> Hidden)
    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    LiftRules.setSiteMap(sitemap)

    // Use jQuery 1.4
    LiftRules.jsArtifacts = net.liftweb.http.js.jquery.JQuery14Artifacts

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))

    ResourceServer.allow {
      case "css" :: _ => true
      case "js" :: _ => true
    }

  }
}
