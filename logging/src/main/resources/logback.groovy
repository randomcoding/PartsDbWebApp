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
import ch.qos.logback.core.status.OnConsoleStatusListener

import static ch.qos.logback.classic.Level.DEBUG
import static ch.qos.logback.classic.Level.INFO
import static ch.qos.logback.classic.Level.WARN

statusListener(OnConsoleStatusListener)

def HOST=hostname;

scan()

appender("STDOUT", ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
	pattern = "%d{HH:mm:ss.SSS} %-5level %logger{10} - %msg%n"
  }
}

logger("*", WARN)

logger("bootstrap.liftweb.Boot", INFO)
logger("uk.co.randomcoding", INFO)

// This seems to need to be ad warn level. If not then there is no log output at all from the startup process
addWarn("Current Host Name: ${HOST}")

def rootLogLevel=INFO;

if (HOST.equalsIgnoreCase("benjymouse")) {
	addInfo("Using logging configuration for ${HOST}")
	logger("uk.co.randomcoding.partsdb.lift.snippet.RecordPayment", DEBUG)
	logger("uk.co.randomcoding.partsdb.lift.snippet.PayInvoices", DEBUG)
	logger("scala", WARN)
	logger("java", WARN)
	logger("org", WARN)
	logger("net", WARN)
	logger("com", WARN)
	logger("ch", WARN)
	
	rootLogLevel=DEBUG
}
else {
	addInfo("Using default logging configuration")
}

addInfo("Setting Root Logger to use ${rootLogLevel} level");

root(rootLogLevel, ["STDOUT"])