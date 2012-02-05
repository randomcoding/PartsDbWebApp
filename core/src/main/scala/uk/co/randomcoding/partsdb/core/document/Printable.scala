/**
 *
 */
package uk.co.randomcoding.partsdb.core.document

/**
 * Identified an entity as able to be printed
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */
trait Printable {
  /**
   * Prints the item as a PDF.
   *
   * @param pdfPath The the file to print the PDF to. This '''should''' be an absolute path.
   */
  def toPdf(pdfPath: String): Unit = {}
}