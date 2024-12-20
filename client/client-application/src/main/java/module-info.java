module cloud.codestore.client.application {
    exports cloud.codestore.client.application;
    opens cloud.codestore.client.application;

    requires cloud.codestore.client.useCases;
    requires cloud.codestore.client.repositories;
    requires cloud.codestore.client.ui;

    // Spring
    requires spring.context;
    requires spring.beans;

    // @Nonnull / @Nullable
    requires jsr305;

    // Logging
    requires org.apache.logging.log4j;

    // JavaFx
    requires javafx.graphics;

    // EventBus
    requires com.google.common;
}