/*
 * Copyright (C) 2012 RandomCoder <randomcoder@randomcoding.co.uk>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *    RandomCoder - initial API and implementation and/or initial documentation
 */

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.status.OnConsoleStatusListener

import static ch.qos.logback.classic.Level.*

statusListener(OnConsoleStatusListener)

def HOST = hostname;

def runMode = System.getProperty("run.mode", "debug")

// Only scan the log config if we are not in production mode.
if (!runMode.equals("production")) {
    scan()
}

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) { pattern = "%d{HH:mm:ss.SSS} %-5level %logger{10} - %msg%n" }
}

logger("*", WARN)

logger("scala", WARN)
logger("java", WARN)
logger("org", WARN)
logger("net", WARN)
logger("com", WARN)
logger("ch", WARN)
logger("comet_trace", WARN)

logger("bootstrap.liftweb.Boot", INFO)
logger("uk.co.randomcoding", INFO)

// This seems to need to be at warn level. If not then there is no log output at all from the startup process
addWarn("Current Host Name: ${HOST}")

def rootLogLevel = INFO;

def isTest = System.getProperty("testing", "no")

if (HOST.equalsIgnoreCase("benjymouse")) {
    addInfo("Using logging configuration for ${HOST}")

    // Only log at WARN level for testing by default
    def testingLogs = ["uk.co.randomcoding": WARN]

    def debugLogs = ["uk.co.randomcoding.partsdb.lift.model.document.NewLineItemDataHolder": DEBUG,
            "uk.co.randomcoding.partsdb.lift.util.snippet.LineItemSnippet": DEBUG,
            "uk.co.randomcoding.partsdb.lift.model.document.DocumentDataHolder": DEBUG,
            "uk.co.randomcoding.partsdb.lift.snippet.MenuTitle": DEBUG,
            "uk.co.randomcoding.partsdb.lift.model.document": DEBUG]

    def logs = debugLogs
    if (isTest.equalsIgnoreCase("yes")) {
        addInfo("Using tests logging configuration")
        logs = testingLogs
    }

    logs.each() { key, value -> logger(key, value) }

    rootLogLevel = DEBUG
}
else {
    addInfo("Using default logging configuration")
}

addInfo("Setting Root Logger to use ${rootLogLevel} level");

root(rootLogLevel, ["STDOUT"])
