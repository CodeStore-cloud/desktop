This module contains scripts for creating an executable application bundle.

To create a standalone application that doesn't require an external Java runtime, this module aims to generate a custom
Java runtime which contains only the JDK- and JavaFX-modules required by the application.

The target platform of the runtime may not be the same as the current platform.
To make this work, you need to set the following properties:
- `jdk.path` the path to the platform specific JDK to be used for the runtime (optional - default is JAVA_HOME)
- `javafx.path` the path to the platform specific JavaFX jmods.

`mvn clean package -Djdk.path=<path to jdk> -Djavafx.path=<path to javafx jmods>`
