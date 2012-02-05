/**
 *
 */
package uk.co.randomcoding.partsdb.core.util

class CountryCode(val tag: String, val countryName: String) {
  override def toString = "Country Code: [tag: %s, countryName: %s]".format(tag, countryName)

  override def equals(that: Any): Boolean = {
    if (that.isInstanceOf[CountryCode]) {
      val other = that.asInstanceOf[CountryCode]
      tag == other.tag && countryName == other.countryName
    }
    else {
      false
    }
  }

  override def hashCode(): Int = {
    var hash = getClass.hashCode
    hash += tag.hashCode
    hash += countryName.hashCode
    hash
  }
}
/**
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 */
object CountryCode {
  def apply(tag: String, countryName: String) = new CountryCode(tag, countryName)

  def apply(codeTuple: (String, String)) = new CountryCode(codeTuple._1, codeTuple._2)

  /**
   * Pattern match an address for its country code
   */
  def unapply(addressLines: Seq[String]): Option[CountryCode] = CountryCodes.countryCodeFromAddressLines(addressLines)

  /**
   * Pattern match a string against a country code
   */
  def unapply(countryNameOrTag: String): Option[CountryCode] = CountryCodes.matchToCountryCode(countryNameOrTag)
}