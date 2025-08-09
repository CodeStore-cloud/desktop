package cloud.codestore.core.application.update;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * The dialog that shows the progress of downloading the application.
 */
public class UpdateDialog {
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Stage window;
    private Runnable cancelCallback = () -> {};

    void show() throws IOException {
        URL fxmlFile = getClass().getResource("updateDialog.fxml");
        ResourceBundle resourceBundle = ResourceBundle.getBundle("dialog-messages");
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlFile, resourceBundle);
        fxmlLoader.setControllerFactory(controllerClass -> this);
        Stage window = fxmlLoader.load();
        window.setOnCloseRequest(event -> cancel());
        window.show();
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
