module cloud.codestore.client.repositories {
    exports cloud.codestore.client.repositories;
    exports cloud.codestore.client.repositories.snippets;
    exports cloud.codestore.client.repositories.language;
//    exports cloud.codestore.client.repositories.root;
//    exports cloud.codestore.client.repositories.tags;

    opens cloud.codestore.client.repositories;
    opens cloud.codestore.client.repositories.tags;
    opens cloud.codestore.client.repositories.snippets;
    opens cloud.codestore.client.repositories.root;
    opens cloud.codestore.client.repositories.language;

    requires cloud.codestore.client.useCases;

    // JSON:API
    requires cloud.codestore.jsonapi;

    // REST Client
    requires spring.core;
    requires spring.web;
    requires spring.webflux;
    requires reactor.core;

    // @Nonnull / @Nullable
    requires jsr305;

}