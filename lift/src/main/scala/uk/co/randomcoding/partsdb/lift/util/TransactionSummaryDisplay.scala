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