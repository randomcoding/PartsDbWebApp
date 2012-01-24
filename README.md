# Parts Database Web App
This project uses [sbt 0.11.x](http://github.com/harrah/xsbt) to build.

## Eclipse Integration
Integration with eclipse is done via [eclipsesbt](https://github.com/typesafehub/sbteclipse).

In order to update the project configuration do the following:
1. Run `eclipse` from the sbt prompt in the root project to generate the eclipse ```.project``` and ```.classpath``` files for each code project.
1. Open Eclipse and add a dependency on the db project to the lift project. See [this sbteclipse issue](https://github.com/typesafehub/sbteclipse/issues/77).
 * Right click on the lift project: 'Properties -> Java Build Path -> Projects -> Add... -> Select db project -> Ok`

## Eclipse Configuration
As the sbt configuration is used as the master configuration for the eclipse build settings, and the settings file is used to store formatting, import managementand other settings as well as the build settings we need to set the non compiler settings in Eclipse as global configuration. See [this sbteclipse issue](https://github.com/typesafehub/sbteclipse/issues/82).

### Formatter
The Scala formatter settings can be left as is, with the exception of ensuring that `Use Compact Control Readability Style` is checked in the `Miscellaneous` tab.

### Syntax Highlights
These are entirely up to you.

### Compiler
Anything you want set here should be configured in the `scalacOptions` for the project build and will be added by `sbteclipse` when the `eclipse` task is run.

### Organize Imports

* Set the `collapse into single import statement option`
* Add the following to the **Always use wildcard imports...** area:
 * net.liftweb.util.Helpers
 * net.liftweb.http.SHtml
 * com.mongodb.casbah.Imports (soon to be removed)
 * uk.co.randomcoding.partsdb.db.mongo.MongoUserAccess
 * uk.co.randomcoding.partsdb.db.util.Helpers

## Building the Project
In order to build the project, simply run ```sbt``` or ```sbt.bat``` to enter the sbt prompt. From there run ```test``` to run the project tests. To run the jetty webapp enter the following:
```$>project lift
$>container:start```
This is required as you need to run the ```container:start``` task from within the ```lift``` project. This is done by the ```project lift``` command.

Alternatively, use the `start-webapp` alias.

There are aliases to the following tasks that can be run from within any project context:
* start-webapp (or run-webapp) - This will start the web container if it is not already running
* stop-webapp - Stops a running webapp
* restart-webapp - Can you guess :)
* test-core, test-db, test-lift - Run the tests in the particular project
* test-clean-core, test-clean-db, test-clean-lift - Clean and then run the tests in the given project.

## Documentation
Project Scaladoc can be found at http://randomcoding.github.com/PartsDbWebApp/scaladoc/current/

