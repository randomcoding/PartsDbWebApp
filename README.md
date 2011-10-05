# Parts Database Web App
This project uses [sbt 0.11.0](http://github.com/harrah/xsbt) to build its three sub-modules.

## Eclipse Integration
Integration with eclipse is done via [eclipsesbt](https://github.com/typesafehub/sbteclipse). Simply run ```eclipse skip-root``` from the sbt prompt in the root project to generate the eclipse ```.project``` and ```.classpath``` files for each code project.
If you run the ```eclipse create-src``` task then you will need to remove the ```src/main/java``` and ```src/test/java``` directories from each project. *this istemporary until we have generated source code for each project.*

## Building the Project
In order to build the project, simply run ```sbt``` or ```sbt.bat``` to enter the sbt prompt. From there run ```test``` to run the project tests. To run the jetty webapp enter the following:
```$>project lift
$>jetty-run```
This is required as you need to run the ```jetty-run``` task from within the ```lift``` project. This is done by the ```project lift``` command.

