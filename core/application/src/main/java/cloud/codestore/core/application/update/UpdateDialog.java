package cloud.codestore.core.application.update;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

import javax.annotation.Nonnull;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * The dialog that shows the progress of downloading the application.
 */
public class UpdateDialog {
    private static final String FXML_FILE_NAME = "updateDialog.fxml";

    @FXML
    private ProgressBar progressBar;
    @FXML
    private Stage window;
    private Runnable cancelCallback = () -> {};

    /**
     * Shows this dialog.
     * This method waits until the dialog is visible.
     * @throws ExecutionException if the FXML file could not be loaded.
     * @throws InterruptedException if the thread was interrupted while waiting for the dialog to be visible.
     */
    void show() throws ExecutionException, InterruptedException {
        URL fxmlFile = getClass().getResource(FXML_FILE_NAME);
        Objects.requireNonNull(fxmlFile, "Cannot find " + FXML_FILE_NAME);
        ResourceBundle resourceBundle = ResourceBundle.getBundle("dialog-messages");
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlFile, resourceBundle);
        fxmlLoader.setControllerFactory(controllerClass -> this);

        CompletableFuture<Void> dialogVisibility = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                Stage window = fxmlLoader.load();
                window.setOnCloseRequest(event -> cancel());
                window.show();
                dialogVisibility.complete(null);
            } catch (Throwable exception) {
                dialogVisibility.completeExceptionally(exception);
            }
        });

        dialogVisibility.get();
    }

    void close() {
        Platform.runLater(window::close);
    }

    /**
     * Sets the download progress.
     *
     * @param progress the progress between 0 and 1
     */
    void setProgress(double progress) {
        progressBar.setProgress(progress);
    }

    /**
     * @param callback a function to be called whenever the user clicks "cancel".
     */
    void onCancel(@Nonnull Runnable callback) {
        this.cancelCallback = callback;
    }

    @FXML
    private void cancel() {
        cancelCallback.run();
    }
}
