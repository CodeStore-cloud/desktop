module cloud.codestore.client.ui {
    exports cloud.codestore.client.ui;
    exports cloud.codestore.client.ui.icon;
    exports cloud.codestore.client.ui.separator;
    exports cloud.codestore.client.ui.footer;
    exports cloud.codestore.client.ui.selection;
    exports cloud.codestore.client.ui.selection.filter;
    exports cloud.codestore.client.ui.selection.sort;
    exports cloud.codestore.client.ui.snippet.title;
    exports cloud.codestore.client.ui.snippet.description;
    exports cloud.codestore.client.ui.snippet.details;
    exports cloud.codestore.client.ui.snippet.footer;

    opens cloud.codestore.client.ui.history;
    opens cloud.codestore.client.ui.selection;
    opens cloud.codestore.client.ui.selection.filter;
    opens cloud.codestore.client.ui.selection.sort;
    opens cloud.codestore.client.ui.selection.list;
    opens cloud.codestore.client.ui.selection.search;
    opens cloud.codestore.client.ui.snippet;
    opens cloud.codestore.client.ui.snippet.title;
    opens cloud.codestore.client.ui.snippet.description;
    opens cloud.codestore.client.ui.snippet.code;
    opens cloud.codestore.client.ui.snippet.details;
    opens cloud.codestore.client.ui.snippet.footer;

    requires cloud.codestore.client.useCases;

    // JavaFX
    requires javafx.fxml;
    requires javafx.web;

    // @Nonnull / @Nullable
    requires jsr305;

    // Logging
    requires org.apache.logging.log4j.core;

    // EventBus
    requires com.google.common;

    // Splash Screen
    requires java.desktop;
}