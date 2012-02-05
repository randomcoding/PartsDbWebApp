/*
 * Sets JQuery to style certain input types as buttons.
 * 
 * This currently sets all button elements, inputs with type=submit
 * and <a> tags with a class of btn to render as buttons
 */
$(function() {
	$("input:submit, button, a", ".btn").button();
});