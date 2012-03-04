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

  def tabDisplayScript = Script(JsRaw("""<script>
	$(function() {
	  $("#tabs").tabs({
	    fx: {
		  opacity: 'toggle'
		}
      });
    });
  </script>"""))
}