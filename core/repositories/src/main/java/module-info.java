module cloud.codestore.core.repositories {
    exports cloud.codestore.core.repositories;
    exports cloud.codestore.core.repositories.snippets;
    exports cloud.codestore.core.repositories.tags;

    opens cloud.codestore.core.repositories;
    opens cloud.codestore.core.repositories.snippets;

    requires cloud.codestore.core.useCases;

    // JSON-Object-Mapper
    requires com.fasterxml.jackson.databind;

    // Indexing
    requires org.apache.lucene.core;
    requires org.apache.lucene.analysis.common;

    // Spring
    requires spring.context;
    requires spring.beans;

    // @Nonnull / @Nullable
    requires jsr305;

    // Logging
    requires org.slf4j;
}