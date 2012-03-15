/**
 *
 */
package uk.co.randomcoding.partsdb.lift.snippet

import uk.co.randomcoding.partsdb.lift.util.snippet.StatefulValidatingErrorDisplaySnippet
import net.liftweb.http.S
import net.liftweb.util.Helpers._
import net.liftweb.common.Full
import uk.co.randomcoding.partsdb.core.document.Document
import org.bson.types.ObjectId
import uk.co.randomcoding.partsdb.lift.util.snippet.TransactionSnippet
import scala.xml.Text
import uk.co.randomcoding.partsdb.core.document.DocumentType

/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
class AddEditDelivery extends StatefulValidatingErrorDisplaySnippet with TransactionSnippet {

  private[this] lazy val deliveryNotes = documentsOfType(DocumentType.DeliveryNote)
  private[this] lazy val orders = documentsOfType(DocumentType.Order)

  override def dispatch = {
    case "render" => render
  }

  def render = {
    "#title" #> Text("Create Delivery Note") &
      renderTransactionDetails()
  }

  override val validationItems = Nil
}