/**
 *
 */
package uk.co.randomcoding.partsdb.core.address

import uk.co.randomcoding.partsdb.core.id.{ Identifier, Identifiable }
import uk.co.randomcoding.partsdb.core.util.CountryCodes._

/**
 * @constructor Create a new address object
 * @param id The [[uk.co.randomcoding.partsdb.core.address.AddressId]] of this address. This is used for internal referencing of address objects from other entities.
 * @param shortName The short (friendly) name of this Address
 * @param addressText The plain text version of the address
 * @param country The country this address is in
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
case class Address(val addressId: Identifier, val shortName: String, val addressText: String, val country: String) extends Identifiable {
  override val identifierFieldName = "addressId"

  //override def id = addressId.id
}

/**
 * Address Companion object. Provides additional matching capability for Addresses in different formats
 */
object Address {

  def apply(addressString: String): Option[Address] = {
    apply(addressString.split(",") map (_ trim))
  }
  /**
   * Matches a list of strings, one address entry per line, to an [[uk.co.randomcoding.partsdb.core.address.Address]]
   *
   * This currently works by extracting the Country code from the address lines and
   * @return
   */
  def apply(addressLines: Seq[String]): Option[Address] = {
    val country = identifyCountry(addressLines)

    countryCodes.find(_._1 == country) match {
      case None => None
      case Some(code) => Some(Address(Identifier(0), addressLines(0), addressLines.mkString(",\n").trim, code._2))
    }
  }

  /**
   * Gets the country code from the address.
   *
   * Currently this will check for a UK Post Code and return UK, otherwise will return the last line of the input list
   *
   * This is the key method as it is used by the match in apply to determine if an address is valid.
   *
   * This needs a better means of country identification
   */
  private def identifyCountry(addressLines: Seq[String]) = if (hasPostCode(addressLines)) "UK" else addressLines.last

  private val postCodeRegex = """[A-Z]{1,2}[1-9][0-9]? [0-9][A-Z]{2}""".r
  private val zipCodeRegex = """[0-9]{5,6""".r

  private val zipCodeOption = (addressLine: String) => zipCodeRegex.findFirstIn(addressLine)

  private val postCodeOption = (addressLine: String) => postCodeRegex.findFirstIn(addressLine)

  private val isCode = (line: String, codeMatchOption: (String => Option[String])) => codeMatchOption(line) isDefined

  private val hasPostCode = (addressLines: Seq[String]) => addressLines.find(isCode(_, postCodeOption)).isDefined
}

object NullAddress extends Address(Identifier(-1), "", "", "")