package cloud.codestore.core.application.update;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * An error dialog that is shown if updating the application fails.
 * It also provides the possibility to report the error.
 */
public class ErrorDialog {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorDialog.class);

    @FXML
    private Stage window;
    private final Throwable exception;

    ErrorDialog(Throwable exception) {
        this.exception = exception;
    }

    void show() {
        URL fxmlFile = getClass().getResource("errorDialog.fxml");
        ResourceBundle resourceBundle = ResourceBundle.getBundle("dialog-messages");
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlFile, resourceBundle);
        fxmlLoader.setControllerFactory(controllerClass -> this);

        try {
            window = fxmlLoader.load();
            window.show();
        } catch (IOException exception) {
            LOGGER.error("Failed to show error dialog. Visiting homepage...", exception);
            visitHomepage();
        }
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
        // TODO send error report
        window.close();
    }
}
