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
object TransactionSummaryDisplay {

  def apply(transactions: Seq[Transaction]) = transactions map (transaction => {
    "#transactionName" #> Text("Transaction Name TBD") &
      "#transactionStarted" #> Text(new DateTime(transaction.creationDate.get).toString("dd/MM/yyyy")) &
      "#display" #> link("/display/Transaction?id=%s".format(transaction.id.get), () => (), Text("Display"))
  })
}