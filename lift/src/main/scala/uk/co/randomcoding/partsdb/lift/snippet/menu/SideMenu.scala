/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet.menu

import net.liftweb.http.S
import net.liftweb.sitemap._
import scala.xml._
import net.liftweb.common.Logger

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class SideMenu extends Logger {
  def builder: NodeSeq = {
    S.request.map(req => {
      val menu = req.buildMenu
      trace("Menu: %s".format(menu))
      trace("Menu Lines: %s".format(menu.lines))
      menu.lines match {
        case Nil => Text("No Navigation Defined.")
        case x :: xs => <ul id="navlist">{ (x :: xs).flatMap(buildANavItem(_)) }</ul>
      }
    }).openOr(Text("No Navigation Defined."))
  }

  private def buildANavItem(i: MenuItem) = i match {
    case MenuItem(text, uri, _, true, _, _) => (<li><a href={ uri } class="active button"><span>{ text }</span></a></li>)
    case MenuItem(text, uri, _, _, true, _) => (<li><a href={ uri } class="active button"><span>{ text }</span></a></li>)
    case MenuItem(text, uri, _, _, _, _) => (<li><a class="button" href={ uri }><span>{ text }</span></a></li>)
  }
}