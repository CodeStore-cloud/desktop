# Bundling {CodeStore}

This module contains scripts for creating an executable application bundle.

## Custom Java Runtime
To create a standalone application that doesn't require an external Java runtime, this module aims to generate a custom
Java runtime which contains only the JDK- and JavaFX-modules required by the application.

The target platform of the runtime may not be the same as the current platform.
To make this work, you need to set the following properties:

- `jdk.path` the path to the platform specific JDK to be used for the runtime (optional - default is JAVA_HOME)
- `javafx.path` the path to the platform specific JavaFX jmods.

`mvn clean package -Djdk.path=<path to jdk> -Djavafx.path=<path to javafx jmods>`

## Windows Installation File

On Windows, an installation file can be created using [Inno Setup](https://jrsoftware.org/isinfo.php).
To perform this step, you need to set the property `inno.setup.path`, which contains the path to the Inno Setup
installation folder on your system. If the property is not set, this is omitted.

`mvn clean package -Djdk.path=<path to jdk> -Djavafx.path=<path to javafx jmods> -Dinno.setup.path=<path to Inno Setup>`
