package cloud.codestore.core.application.update;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ResourceBundle;

/**
 * The dialog that shows the progress of downloading the application.
 */
public class UpdateDialog extends Application {
    private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("dialog-messages");
    private static final int PADDING = 10;
    private static UpdateDialog INSTANCE;

    private Runnable cancelCallback = () -> {};
    private ProgressBar progressBar;
    private Stage window;

    static UpdateDialog show() {
        Thread thread = new Thread(Application::launch);
        thread.setDaemon(true);
        thread.start();

        waitForDialogToBeVisible();
        return INSTANCE;
    }

    @Override
    public void init() {
        INSTANCE = this;
    }

    @Override
    public void start(Stage window) {
        this.window = window;
        window.setTitle(MESSAGES.getString("dialog.update.progress.title"));
        window.setOnCloseRequest(event -> cancelCallback.run());

        Pane rootPane = createRootPane();
        Scene scene = new Scene(rootPane, rootPane.getPrefWidth(), rootPane.getPrefHeight());
        window.setScene(scene);
        window.setResizable(false);
        window.show();
    }

    /**
     * Sets the download progress.
     * @param progress the progress between 0 and 1
     */
    void setProgress(double progress) {
        progressBar.setProgress(progress);
    }

    /**
     * @param callback a function to be called whenever the user clicks "cancel".
     */
    void onCancel(Runnable callback) {
        this.cancelCallback = callback;
    }

    void close() {
        Platform.runLater(window::close);
    }

    private Pane createRootPane() {
        VBox pane = new VBox();
        pane.setPadding(new Insets(PADDING));
        pane.setSpacing(PADDING);

        Label updateMessage = new Label(MESSAGES.getString("dialog.update.progress.message"));
        Label dialogText = new Label(MESSAGES.getString("dialog.update.progress.autoRestart"));
        progressBar = new ProgressBar();
        progressBar.setProgress(0);
        progressBar.setMinHeight(progressBar.getPrefHeight());
        progressBar.setMaxWidth(Double.MAX_VALUE);

        ButtonBar buttonBar = new ButtonBar();
        Button cancelButton = new Button(MESSAGES.getString("dialog.update.progress.cancel"));
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(event -> cancelCallback.run());
        buttonBar.getButtons().addAll(cancelButton);

        pane.getChildren().addAll(updateMessage, dialogText, progressBar, buttonBar);
        VBox.setVgrow(progressBar, Priority.ALWAYS);
        cancelButton.setAlignment(Pos.BOTTOM_RIGHT);

        return pane;
    }

    private static void waitForDialogToBeVisible() {
        while (INSTANCE == null || !INSTANCE.window.isShowing()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(exception);
            }
        }
    }
}
