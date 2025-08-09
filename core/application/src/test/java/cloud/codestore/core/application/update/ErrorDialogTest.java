package cloud.codestore.core.application.update;

import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@DisplayName("The error dialog")
class ErrorDialogTest extends ApplicationTest {
    private final Exception exception = new IOException("Something went wrong...");
    private final ErrorDialog errorDialog = new ErrorDialog(exception);

    @Start
    public void start(Stage stage) throws Exception {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("dialog-messages");
        URL fxmlFile = getClass().getResource("errorDialog.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlFile, resourceBundle);
        fxmlLoader.setControllerFactory(controllerClass -> errorDialog);
        Stage window = fxmlLoader.load();
        window.show();
    }

    @Test
    @DisplayName("sends an error report when the user clicks \"report error\"")
    void sendErrorReport() {
        clickOn(".button");
        Assertions.fail();
    }
}