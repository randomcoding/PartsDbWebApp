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
package uk.co.randomcoding.partsdb.lift.util

import uk.co.randomcoding.partsdb.core.document.Document
import scala.xml.NodeSeq
import uk.co.randomcoding.partsdb.core.transaction.Transaction
import net.liftweb.util.Helpers._
import scala.xml.Text
import org.joda.time.DateTime
import net.liftweb.http.SHtml._

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object TransactionSummaryDisplay extends TabularEntityDisplay {
  override type EntityType = Transaction

  override val addEditColumn = false

  override val rowHeadings = List("Transaction Name", "Started On")

  override def displayEntity(transaction: Transaction, editLink: Boolean = false, displayLink: Boolean = true) = {
    val displayLink = link("display/transaction?id=".format(transaction.id.get.toString), () => (), Text("Display"))

    <td>{ transaction.shortName }</td> ++
      <td>{ new DateTime(transaction.creationDate.get).toString("dd/MM/yyyy") }</td> ++
      addDisplayCellOnly("Transaction", transaction.id.get)
  }

}
