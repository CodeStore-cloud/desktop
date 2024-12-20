module cloud.codestore.core.application {
    exports cloud.codestore.core.application;
    opens cloud.codestore.core.application to spring.core;

    requires cloud.codestore.core.useCases;
    requires cloud.codestore.core.repositories;
    requires cloud.codestore.core.api;

    // Spring Boot
    requires spring.boot;
    requires spring.context;
    requires spring.beans;
    requires spring.boot.autoconfigure;
}