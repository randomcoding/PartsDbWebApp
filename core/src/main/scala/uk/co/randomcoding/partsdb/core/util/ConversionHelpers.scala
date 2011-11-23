/**
 *
 */
package uk.co.randomcoding.partsdb.core.util

import uk.co.randomcoding.partsdb.core.address.{ NullAddress, Address }
import uk.co.randomcoding.partsdb.core.id.Identifier

import net.liftweb.common.Logger

/**
 * Implicit conversions for common types to core object types
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object ConversionHelpers extends Logger {

  /**
   * Conversion for a free text address to a [[uk.co.randomcoding.partsdb.core.address.Address]].
   *
   * @param addressText The input text to parse
   * @return An [[uk.co.randomcoding.partsdb.core.address.Address]] object with the relevant details or a [[uk.co.randomcoding.partsdb.core.address.NullAddress]] if the parse failed.
   * The generated [[uk.co.randomcoding.partsdb.core.address.Address]] will have an `addressId` of [[uk.co.randomcoding.partsdb.core.id.Identifier]]`(0)`.
   */
  implicit def parseAddressText(addressText: String): Address = {
    val addressLines = scala.io.Source.fromString(addressText).getLines.toList map (removeTrailingComma(_))
    if (isValidAddress(addressLines)) {
      try {
        val shortName = addressLines(0)
        val country = addressLines.last // TODO: validate this is a country code
        Address(Identifier(0), shortName, addressText, country)
      }
      catch {
        case t: Throwable =>
          {
            error("Failed to parse %s as an address".format(addressText), t)
          }
      }
    }

    NullAddress

  }

  /**
   * Determines if an address is valid.
   *
   * @todo Create an address format extractor class that will extract the details we want and validate the address in one go
   *
   * For now this looks for a post code or a 5 or 6 digit zip code. This needs to be worked on.
   */
  def isValidAddress(addressLines: List[String]): Boolean = {
    val postCode = addressLines.find("""[A-Z]{1,2}[1-9][0-9]? [0-9][A-Z]{2}""".r.findFirstIn(_).isDefined).headOption
    val zipCode = addressLines.find("""[0-9]{5,6}""".r.findFirstIn(_).isDefined).headOption

    postCode.isDefined || zipCode.isDefined
  }

  private def removeTrailingComma = (line: String) => {
    val trimmed = line.trim
    if (trimmed endsWith ",") trimmed.take(trimmed.length - 1) else trimmed
  }
}