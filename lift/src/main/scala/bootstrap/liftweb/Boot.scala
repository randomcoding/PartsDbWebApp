package bootstrap.liftweb

import net.liftweb._
import util._
import Helpers._
import common._
import http._
import sitemap._
import Loc._
import net.liftweb.http.auth.{ HttpBasicAuthentication, AuthRole }
import http.ParsePath
import auth._

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot extends Loggable {
  def boot {
    // where to search snippet
    LiftRules.addToPackages("uk.co.randomcoding.partsdb.lift")

    // authentication
    LiftRules.httpAuthProtectedResource.prepend {
      case (Req("/" :: _, _, _)) => Full(AuthRole("user"))
    }

    LiftRules.authentication = HttpBasicAuthentication("AM2") {
      // TODO: Add user access code here
      case ("Am2User", "Am2aM2", req) => {
        logger.info("You are now authenticated !")
        userRoles(AuthRole("user"))
        true
      }
    }

    /*
     * This provides access control to pages. 
     * In order to allow a page, add an entry here
     */
    def sitemap = SiteMap(
      Menu.i("Home") / "index",
      Menu.i("Customers") / "customers",
      Menu.i("Parts") / "parts",
      Menu.i("Suppliers") / "suppliers",
      // hidden entries
      Menu.i("Add Customer") / "addCustomer" >> Hidden,
      Menu.i("Add Part") / "addPart" >> Hidden,
      Menu.i("Add Supplier") / "addSupplier" >> Hidden)

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
