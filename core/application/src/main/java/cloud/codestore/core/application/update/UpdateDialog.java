package cloud.codestore.core.application.update;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

import javax.annotation.Nonnull;
import java.io.IOException;
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
     */
    void show() throws ExecutionException, InterruptedException, IOException {
        URL fxmlFile = getClass().getResource(FXML_FILE_NAME);
        Objects.requireNonNull(fxmlFile, "Cannot find " + FXML_FILE_NAME);
        ResourceBundle resourceBundle = ResourceBundle.getBundle("dialog-messages");
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlFile, resourceBundle);
        fxmlLoader.setControllerFactory(controllerClass -> this);
        showAndWaitForVisibility(fxmlLoader);
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

    private void showAndWaitForVisibility(
            FXMLLoader fxmlLoader
    ) throws ExecutionException, InterruptedException, IOException {
        if (Platform.isFxApplicationThread()) {
            show(fxmlLoader);
        } else {
            CompletableFuture<Void> dialogVisibility = new CompletableFuture<>();
            Platform.runLater(() -> {
                try {
                    show(fxmlLoader);
                    dialogVisibility.complete(null);
                } catch (Throwable exception) {
                    dialogVisibility.completeExceptionally(exception);
                }
            });

            dialogVisibility.get();
        }
    }

    private void show(FXMLLoader fxmlLoader) throws IOException {
        Stage stage = fxmlLoader.load();
        stage.show();
    }
}
