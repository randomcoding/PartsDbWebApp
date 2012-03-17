// Logging configuration, written in Groovy!
//
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

import static ch.qos.logback.classic.Level.DEBUG
import static ch.qos.logback.classic.Level.INFO
import static ch.qos.logback.classic.Level.WARN
import static ch.qos.logback.classic.Level.TRACE
import static ch.qos.logback.classic.Level.ALL

appender("STDOUT", ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
	pattern = "%d{HH:mm:ss.SSS}  %-5level %logger{10} - %msg%n"
  }
}

scan()

logger("com", WARN)
logger("org", WARN)
logger("scala", WARN)
logger("java", WARN)
logger("net", WARN)
logger("uk", WARN)
logger("ch", WARN)

logger("uk.co.randomcoding.partsdb", INFO)
logger("uk.co.randomcoding.partsdb.lift.snippet", DEBUG)
logger("uk.co.randomcoding.partsdb.lift.snippet.display", DEBUG)
logger("uk.co.randomcoding.partsdb.lift.util.snippet", DEBUG)

root(WARN, ["STDOUT"])