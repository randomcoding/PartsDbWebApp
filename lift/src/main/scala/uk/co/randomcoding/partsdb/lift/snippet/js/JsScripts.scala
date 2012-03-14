/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet.js

import net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object JsScripts {

  val tabDisplayScript = Script(JsRaw("""$(function() {
	  $("#tabs").tabs({
	    fx: {
		  opacity: 'toggle'
		}
      });
    });"""))

  val accordionScript = Script(JsRaw("""$function() {
      $("#accordion").accordion({
        autoHeight: false;
        collapsible: true;
        active: false;
      });
    });"""))
}