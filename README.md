# Parts Database Web App
This project uses [sbt 0.11.x](http://github.com/harrah/xsbt) to build.

## Eclipse Integration
Integration with eclipse is done via [eclipsesbt](https://github.com/typesafehub/sbteclipse). Simply run ```eclipse skip-root``` from the sbt prompt in the root project to generate the eclipse ```.project``` and ```.classpath``` files for each code project.

## Building the Project
In order to build the project, simply run ```sbt``` or ```sbt.bat``` to enter the sbt prompt. From there run ```test``` to run the project tests. To run the jetty webapp enter the following:
```$>project lift
$>container:start```
This is required as you need to run the ```container:start``` task from within the ```lift``` project. This is done by the ```project lift``` command.

Alternatively, use the start-webapp alias.

There are aliases to the following tasks that can be run from within any project context:
* start-webapp (or run-webapp) - This will start the web container if it is not already running
* stop-webapp - Stops a running webapp
* restart-webapp - Can you guess :)
* test-core, test-db, test-lift - Run the tests in the particular project
* test-clean-core, test-clean-db, test-clean-lift - Clean and then run the tests in the given project.

## Documentation
Project Scaladoc can be found at http://randomcoding.github.com/PartsDbWebApp/scaladoc/current/

