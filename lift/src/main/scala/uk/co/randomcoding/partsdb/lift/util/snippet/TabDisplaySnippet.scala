/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet

import scala.xml.NodeSeq

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait TabDisplaySnippet {

  val tabTitles: Seq[(String, String)]

  private[this] val anchorRef = (anchor: String) => "#" + anchor

  def generateTabs(): NodeSeq = {
    val tabs = tabTitles flatMap (title => {
      <li><a href={ anchorRef(title._1) }>{ title._2 }</a></li>
    })

    <ul> { tabs } </ul>
  }
}