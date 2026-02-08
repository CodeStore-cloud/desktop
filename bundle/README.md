# Bundling {CodeStore}

This module contains scripts for creating an executable application bundle.

## Custom Java Runtime
To create a standalone application that doesn't require an external Java runtime, this module aims to generate a custom
Java runtime which contains only the JDK- and JavaFX-modules required by the application.
Additionally, it bundles the final application as Windows executable or Debian package.

The target platform of the runtime may not be the same as the current platform.
So you need to specify the target platform `windows` or `linux` as Maven profile.

## Windows Installation File

On Windows, an installation file can be created using [Inno Setup](https://jrsoftware.org/isinfo.php).
To perform this step, you need to set the property `inno.setup.path`, which contains the path to the Inno Setup
installation folder on your system. If the property is not set, this is omitted.

`mvn clean package -Pwindows -Dinno.setup.path=<path to Inno Setup>`

## Linux Debian Package
Creating a Debian package will work on Linux as well as Windows.
On Windows, Docker is used to create the package. So make sure that Docker is installed and ready to go.

`mvn clean package -Plinux`
