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
package uk.co.randomcoding.partsdb.lift.snippet

import net.liftweb.http.S
import net.liftweb.common.Full
import net.liftweb.util.Helpers._
import uk.co.randomcoding.partsdb.core.customer.Customer
import uk.co.randomcoding.partsdb.core.util.MongoHelpers._
import uk.co.randomcoding.partsdb.core.part.Part
import uk.co.randomcoding.partsdb.core.part.PartKit
import uk.co.randomcoding.partsdb.core.supplier.Supplier
import uk.co.randomcoding.partsdb.core.vehicle.Vehicle
import uk.co.randomcoding.partsdb.core.document.Quote
import uk.co.randomcoding.partsdb.core.document.Document
import net.liftweb.common.Logger
import uk.co.randomcoding.partsdb.core.transaction.Transaction

/**
 * Generates the title for a page based on the URL and the query parameters.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object MenuTitle extends Logger {
  private[this] val titlePrefix = "C.A.T.9 Application:"

  def render = {
    val uri = S.request match {
      case Full(req) => req.request.uri
      case _ => "Unknonw URL"
    }

    val queryParams: Map[String, String] = S.queryString match {
      case Full(query) => query.split("&").map(qParam => {
        val parts = qParam.split("=")
        debug("Param Parts: %s".format(parts.mkString("[", ", ", "]")))
        (parts(0), parts(1))
      }).toMap
      case _ => Map.empty
    }

    debug("Query Params: %s".format(if (queryParams.isEmpty) "Empty" else queryParams.mkString("[", ", ", "]")))

    "*" #> <title>{ "%s %s".format(titlePrefix, titleForPage(uri, queryParams)) }</title>
  }

  private[this] def titleForPage(pageUrl: String, queryParams: Map[String, String]): String = {

    debug("Page URL: %s".format(pageUrl))
    val urlParts = pageUrl.split("/").toList.drop(1)
    debug("URL Parts: %s".format(urlParts.mkString(", ")))

    urlParts match {
      case "admin" :: Nil => "Admin Pages"
      case "app" :: Nil => "Home"
      case "app" :: "show" :: appPath => "Show %ss".format(queryParams("entityType"))
      case "app" :: "display" :: entityType :: Nil => "Display - %s".format(entityNameForTitle(entityType, queryParams("id")))
      case "app" :: "quote" :: Nil => titleForQuotePage(queryParams)
      case "app" :: "order" :: Nil => "New Order"
      case "app" :: "delivery" :: Nil => "New Delivery Note"
      case "app" :: "invoice" :: Nil => "New Invoice"
      case "app" :: "recordPayment" :: Nil => "Record Payments"
      case "app" :: "payInvoices" :: Nil => "Pay Invoices"
      case "app" :: "print" :: "printdocument" :: Nil => "Print - %s".format(titleForPrintDocument(queryParams.get("documentId")))
      case "app" :: entityType :: Nil => queryParams.get("id") match {
        case Some(id) => "Edit - %s".format(entityNameForTitle(entityType, id))
        case _ => "New %s".format(upperTitle(entityType))
      }
      case Nil => "Login"
      case other => "Unknown path: %s".format(other.mkString("/"))
    }
  }

  private[this] def titleForPrintDocument(docIdParam: Option[String]) = docIdParam match {
    case Some(docId) => Document.findById(docId) match {
      case Some(doc) => doc.documentNumber
      case _ => "Unknown Document"
    }
    case _ => "Unknown Document"
  }

  private[this] def upperTitle(name: String) = name match {
    case "supplier" => "Supplier"
    case "vehicle" => "Vehicle"
    case "part" => "Part"
    case "partkit" => "Part Kit"
    case "customer" => "Customer"
  }

  private[this] def titleForQuotePage(queryParams: Map[String, String]) = {
    queryParams.isEmpty match {
      case true => "New Quote"
      case _ => Document.findById(queryParams("id")) match {
        case Some(doc) => "Edit Quote %s".format(doc.documentNumber)
        case _ => "Edit Unknown Quote"
      }
    }
  }

  private[this] def entityNameForTitle(entityType: String, entityId: String): String = {
    val unknown = (entType: String) => "Unknown %s".format(entType)

    entityType.toLowerCase match {
      case "customer" => Customer.findById(entityId) match {
        case Some(cust) => cust.customerName.get
        case _ => unknown(entityType)
      }
      case "part" => Part.findById(entityId) match {
        case Some(part) => part.partName.get
        case _ => unknown(entityType)
      }
      case "partkit" => PartKit.findById(entityId) match {
        case Some(partKit) => partKit.kitName.get
        case _ => unknown(entityType)
      }
      case "supplier" => Supplier.findById(entityId) match {
        case Some(supplier) => supplier.supplierName.get
        case _ => unknown(entityType)
      }
      case "vehicle" => Vehicle.findById(entityId) match {
        case Some(vehicle) => vehicle.vehicleName.get
        case _ => unknown(entityType)
      }
      case "transaction" => Transaction.findById(entityId) match {
        case Some(trn) => trn.shortName
        case _ => unknown(entityType)
      }
    }
  }

}
