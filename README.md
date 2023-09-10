# {CodeStore} 2.0

This is the open source reimplementation of the [{CodeStore}](https://codestore.cloud) desktop application.

**This project is in a very early state of the development. It does not provide all functionalities yet.*

## REST Interface

Beside the graphical user interface, the application provides a REST interface which allows external applications
to use basic functions like creating, editing and deleting code snippets.
That way, third party developers can create their own user interfaces or IDE plugins and use the basic functionalities
of {CodeStore} by using the {CodeStore} Core API.

By providing an HTTP based API, third party clients can be developed independently of specific programming languages or
libraries.

The application remains to be a local, offline-first desktop application and does not act as a server within a
network. The purpose of the {CodeStore} Core API is to provide inter-process communication on the local machine.

## Architecture

The application is divided into two modules, each running in a separate process: the GUI and the {CodeStore} Core.

### {CodeStore} Core

The {CodeStore} Core contains the basic functionalities like saving, editing and deleting code snippets, as well as
synchronizing code snippets with the {CodeStore} cloud. The data can be accessed via the {CodeStore} Core API.

### Graphical User Interface

The GUI provides user-friendly access to the data managed by the {CodeStore} Core and extends its functionalities by
UI specific functions like syntax-highlighting, autocompletion and quickfilter.
