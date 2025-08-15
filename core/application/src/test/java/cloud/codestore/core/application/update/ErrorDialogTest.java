package cloud.codestore.core.application.update;

import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@DisplayName("The error dialog")
@ExtendWith(MockitoExtension.class)
class ErrorDialogTest extends ApplicationTest {
    @Mock
    private ErrorReporter errorReporter;
    private Exception exception = new IOException("Something went wrong...");
    private ErrorDialog errorDialog;

    @Start
    public void start(Stage stage) throws Exception {
        errorDialog = new ErrorDialog(errorReporter, exception);

        ResourceBundle resourceBundle = ResourceBundle.getBundle("dialog-messages");
        URL fxmlFile = getClass().getResource(ErrorDialog.FXML_FILE_NAME);
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlFile, resourceBundle);
        fxmlLoader.setControllerFactory(controllerClass -> errorDialog);
        Stage window = fxmlLoader.load();
        window.show();
    }

    @Test
    @DisplayName("sends an error report when the user clicks \"report error\"")
    void sendErrorReport() {
        clickOn(".button");
        Mockito.verify(errorReporter).sendReport(exception);
    }
}