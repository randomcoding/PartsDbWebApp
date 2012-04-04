//
// Built on Wed Apr 04 22:53:10 CEST 2012 by logback-translator
// For more information on configuration files in Groovy
// please see http://logback.qos.ch/manual/groovy.html

// For assistance related to this tool or configuration files
// in general, please contact the logback user mailing list at
//    http://qos.ch/mailman/listinfo/logback-user

// For professional support please see
//   http://www.qos.ch/shop/products/professionalSupport

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

import static ch.qos.logback.classic.Level.DEBUG
import static ch.qos.logback.classic.Level.INFO
import static ch.qos.logback.classic.Level.WARN
import ch.qos.logback.core.status.OnConsoleStatusListener

statusListener(OnConsoleStatusListener)

scan()
appender("STDOUT", ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
	pattern = "%d{HH:mm:ss.SSS} %-5level %logger{10} - %msg%n"
  }
}
logger("bootstrap.liftweb.Boot", INFO)
logger("uk.co.randomcoding", INFO)

if (${hostname} == "benjymouse") {
	logger("uk.co.randomcoding.partsdb.lift.snippet.AddEditInvoice", DEBUG)
}

root(WARN, ["STDOUT"])