package cloud.codestore.core.application.update;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * An error dialog that is shown if updating the application fails.
 * It also provides the possibility to report the error.
 */
public class ErrorDialog {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorDialog.class);
    static final String FXML_FILE_NAME = "errorDialog.fxml";

    @FXML
    private Stage window;
    private final ErrorReporter errorReporter;
    private final Throwable error;

    ErrorDialog(@Nonnull ErrorReporter errorReporter, @Nonnull Throwable error) {
        this.errorReporter = errorReporter;
        this.error = error;
    }

    /**
     * Shows this dialog.
     */
    void show() {
        URL fxmlFile = getClass().getResource(FXML_FILE_NAME);
        Objects.requireNonNull(fxmlFile, "Cannot find " + FXML_FILE_NAME);

        ResourceBundle resourceBundle = ResourceBundle.getBundle("dialog-messages");
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlFile, resourceBundle);
        fxmlLoader.setControllerFactory(controllerClass -> this);

        Platform.runLater(() -> {
            try {
                window = fxmlLoader.load();
                window.show();
            } catch (IOException exception) {
                LOGGER.error("Failed to show error dialog. Visiting homepage...", exception);
                visitHomepage();
            }
        });
    }

    @FXML
    private void visitHomepage() {
        try {
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(URI.create("https://codestore.cloud"));
        } catch (IOException ioException) {
            LOGGER.error("Failed to open link to homepage.", ioException);
        }
    }

    @FXML
    private void reportError() {
        errorReporter.sendReport(error);
        window.close();
    }
}
