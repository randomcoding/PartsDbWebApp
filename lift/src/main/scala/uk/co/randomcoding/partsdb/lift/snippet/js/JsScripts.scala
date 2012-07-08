/*
 * Copyright (C) 2012 RandomCoder <randomcoder@randomcoding.co.uk>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *    RandomCoder - initial API and implementation and/or initial documentation
 */
package uk.co.randomcoding.partsdb.lift.snippet.js

import net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._

/**
 * Common versions of JQuery Scripts to embed in pages.
 *
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
