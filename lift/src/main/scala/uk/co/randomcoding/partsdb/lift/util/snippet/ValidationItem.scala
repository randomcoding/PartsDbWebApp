package uk.co.randomcoding.partsdb.lift.util.snippet

/**
 * An item that can be checked for a valid status.
 *
 * @constructor Create a new item to check for validation
 * @param toValidate The actual item to validate
 * @param errorLocationId The id of the element on the web page to display the error message at
 * @param errorMessage The message to display
 */
case class ValidationItem(toValidate: Any, fieldName: String /*, errorMessage: String*/ )