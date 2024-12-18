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
synchronizing code snippets with a cloud service. The data can be accessed via the {CodeStore} Core API.

### Graphical User Interface

The GUI provides user-friendly access to the data managed by the {CodeStore} Core and extends its functionalities by
UI specific functions like syntax-highlighting, autocompletion and quickfilter.

## Clean Architecture

The architecture of both modules is based on Uncle
Bob's [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html).
The basic idea is that the functionality does not depend on the implementation details and thus is independent of the
interface or underlying data management.

Both modules are in turn separated into multiple modules, each serving its own purpose:

```
desktop-app
|
|-- client
|   |-- client-useCases      -> abstract application logic
|   |-- client-ui            -> JavaFX based UI
|   |-- client-repositories  -> access file system and Core API
|   `-- client-application   -> main module and dependency injection
|
|-- core
|   |-- useCases             -> abstract application logic
|   |-- api                  -> REST API
|   |-- repositories         -> access file system and public server
|   `-- application          -> main module and dependency injection
|
`-- bundle                   -> application bundle build scripts
```

## Starting the Application

To start the entire application, first start the {CodeStore} Core and then the client.
Don't forget to set the Spring profile to "dev" in each case
(e.g. by using the "spring.profiles.active" environment variable).
Otherwise, there is a risk to compromise your real data!

The {CodeStore} Core API expects an access-token for security reasons. If you want to play around with the API in
the browser, you have to disable the "server.authentication.required" flag in the Core's application-dev.properties.