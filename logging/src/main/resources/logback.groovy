// Logging configuration, written in Groovy!
//
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

import static ch.qos.logback.classic.Level.DEBUG
import static ch.qos.logback.classic.Level.INFO
import static ch.qos.logback.classic.Level.WARN
import static ch.qos.logback.classic.Level.TRACE

appender("STDOUT", ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
	pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  }
}

// Sample of setting package logging to DEBUG level
//logger("uk.co.randomcoding.partsdb.lift", DEBUG)

root(WARN, ["STDOUT"])