/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import net.liftweb.http.S

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object SimpleRedirect {
  def render = S redirectTo (S.attr("target") openOr (""))
}