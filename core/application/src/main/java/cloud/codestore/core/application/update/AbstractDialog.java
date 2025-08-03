package cloud.codestore.core.application.update;

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

abstract class AbstractDialog {
    private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("dialog-messages");
    private static final int PADDING = 10;

    private CountDownLatch visibilityLatch = new CountDownLatch(1);
    private Runnable onClose = () -> {};
    private Stage window;

    void show() {
        Platform.runLater(() -> {
            if (window == null) {
                window = new Stage();
            }

            window.setTitle(getTitle());
            window.setResizable(false);
            window.setOnCloseRequest(event -> onClose.run());
            window.showingProperty()
                  .addListener((observable, oldValue, newValue) -> visibilityLatch.countDown());

            Pane rootPane = createRootPane();
            Scene scene = new Scene(rootPane, rootPane.getPrefWidth(), rootPane.getPrefHeight());
            window.setScene(scene);
            window.show();
            System.out.println("after show");
        });

        waitForDialogToBeVisible();
        System.out.println("Dialog showing");
    }

    void close() {
        Platform.runLater(window::close);
    }

    String getMessage(String key) {
        return MESSAGES.getString(key);
    }

    void setOnClose(Runnable runnable) {
        this.onClose = runnable;
    }

    abstract String getTitle();

    abstract Node[] getElements();

    abstract Button[] getButtons();

    private void waitForDialogToBeVisible() {
        try {
            visibilityLatch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(exception);
        }
    }

    private Pane createRootPane() {
        VBox pane = new VBox();
        pane.setPadding(new Insets(PADDING));
        pane.setSpacing(PADDING);

        ButtonBar buttonBar = new ButtonBar();
        buttonBar.getButtons().addAll(getButtons());

        pane.getChildren().addAll(getElements());
        pane.getChildren().add(buttonBar);

        return pane;
    }
}
