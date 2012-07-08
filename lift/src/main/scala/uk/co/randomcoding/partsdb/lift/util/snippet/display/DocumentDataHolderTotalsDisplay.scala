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

/**
 *
 */
package uk.co.randomcoding.partsdb.lift.util.snippet.display

import uk.co.randomcoding.partsdb.lift.model.document.DocumentDataHolder

import net.liftweb.http.js.jquery.JqWiringSupport
import net.liftweb.http.WiringUI
import net.liftweb.util.Helpers._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
trait DocumentDataHolderTotalsDisplay {
  val dataHolder: DocumentDataHolder

  def renderDocumentTotals() = {
    "#subtotal *" #> WiringUI.asText(dataHolder.subTotal) &
      "#carriage *" #> WiringUI.asText(dataHolder.carriage) &
      "#vat *" #> WiringUI.asText(dataHolder.vatAmount) &
      "#total *" #> WiringUI.asText(dataHolder.totalCost, JqWiringSupport.fade)
  }
}
