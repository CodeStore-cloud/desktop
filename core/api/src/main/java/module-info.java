module cloud.codestore.core.api {
    exports cloud.codestore.core.api;
    exports cloud.codestore.core.api.languages;
    exports cloud.codestore.core.api.root;
    exports cloud.codestore.core.api.snippets;
    exports cloud.codestore.core.api.tags;

    opens cloud.codestore.core.api;
    opens cloud.codestore.core.api.languages;
    opens cloud.codestore.core.api.root;
    opens cloud.codestore.core.api.security;
    opens cloud.codestore.core.api.snippets;
    opens cloud.codestore.core.api.tags;

    requires cloud.codestore.core.useCases;

    // JSON:API
    requires cloud.codestore.jsonapi;
    requires com.fasterxml.jackson.datatype.jsr310;

    // Spring Boot
    requires spring.core;
    requires spring.boot;
    requires spring.context;
    requires spring.beans;
    requires spring.web;
    requires spring.boot.autoconfigure;
    requires org.apache.tomcat.embed.core;

    // @Nonnull / @Nullable
    requires jsr305;

    // Logging
    requires org.slf4j;
}