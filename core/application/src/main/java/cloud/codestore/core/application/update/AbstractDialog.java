package cloud.codestore.core.application.update;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

public abstract class AbstractDialog {
    private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("dialog-messages");
    private static final int PADDING = 10;

    private CountDownLatch visibilityLatch = new CountDownLatch(1);
    private UI delegate;

    void show() {
        Thread thread = new Thread(() -> UI.show(this));
        thread.setDaemon(true);
        thread.start();

        waitForDialogToBeVisible();
    }

    void close() {
        Platform.runLater(delegate::close);
    }

    String getMessage(String key) {
        return MESSAGES.getString(key);
    }

    void setOnClose(Runnable runnable) {
        delegate.window.setOnCloseRequest(event -> runnable.run());
    }

    HostServices getHostServices() {
        return delegate.getHostServices();
    }

    abstract String getTitle();

    abstract Node[] getElements();

    abstract Button[] getButtons();

    private void onInit(UI delegate) {
        this.delegate = delegate;
        delegate.window.showingProperty()
                       .addListener((observable, oldValue, newValue) -> visibilityLatch.countDown());
    }

    private void waitForDialogToBeVisible() {
        try {
            visibilityLatch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(exception);
        }
    }

    public static class UI extends Application {
        private static AbstractDialog controller;
        private Stage window;

        private static void show(AbstractDialog controller) {
            UI.controller = controller;
            launch();
        }

        @Override
        public void start(Stage window) throws Exception {
            this.window = window;
            controller.onInit(this);
            window.setTitle(controller.getTitle());

            Pane rootPane = createRootPane();
            Scene scene = new Scene(rootPane, rootPane.getPrefWidth(), rootPane.getPrefHeight());
            window.setScene(scene);
            window.setResizable(false);
            window.show();
        }

        private Pane createRootPane() {
            VBox pane = new VBox();
            pane.setPadding(new Insets(PADDING));
            pane.setSpacing(PADDING);

            ButtonBar buttonBar = new ButtonBar();
            buttonBar.getButtons().addAll(controller.getButtons());

            pane.getChildren().addAll(controller.getElements());
            pane.getChildren().add(buttonBar);

            return pane;
        }

        private void close() {
            window.close();
        }
    }
}
