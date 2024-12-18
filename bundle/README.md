This module contains scripts for creating an executable application bundle.

To create a standalone application that doesn't require an external Java runtime, this module aims to generate a custom
Java runtime which contains only the JDK- and JavaFX-modules required by the application.

To make this work, you need to specify the path to the JDK to be used for the custom runtime, as well as the
path to the JavaFX jmods.

`mvn package -Djdk.path=<path to jdk> -Djavafx.path=<path to javafx jmods>`
