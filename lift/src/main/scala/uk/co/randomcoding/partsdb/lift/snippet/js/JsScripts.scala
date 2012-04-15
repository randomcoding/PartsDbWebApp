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

  /**
   * Adds the required javascript to a page to convert an element with an id of **tabs** to a tab display.
   *
   * Follows the default jqueryui format.
   */
  val tabDisplayScript = Script(JsRaw("""$(function() {
	  $("#tabs").tabs({
	    fx: {
		  opacity: 'toggle'
		}
      });
    });"""))

  /**
   * Adds the required javascript to a page to convert an element with an id of **accordion** to an accordion section.
   *
   * Follows the default jqueryui format.
   */
  val accordionScript = Script(JsRaw("""$(function() {
      $("#accordion").accordion({
        autoHeight: false,
        collapsible: true,
        active: false
      });
    });"""))

  /**
   * Adds the required javascript to a page to convert an element with an id of **datepicker** to a
   * datepicker text box with an icon to trigger the date entry.
   *
   * Follows the default jqueryui format.
   */
  val calendarScript = Script(JsRaw("""$(function() {
      $( "#datepicker" ).datepicker({
	  	showOn: "button",
		buttonImage: "/images/calendar.gif",
		buttonImageOnly: true,
        buttonText: "Select Date",
        showAnim: "fadeIn",
        dateFormat: "dd/mm/yy",
        onSelect: function(dateText, inst) {
          $("#datepicker").focus()
        }
	  });
    })"""))
}