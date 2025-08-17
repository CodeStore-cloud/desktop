package cloud.codestore.core.application.update;

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

@DisplayName("The error dialog")
@ExtendWith(MockitoExtension.class)
class ErrorDialogTest extends ApplicationTest {
    @Mock
    private ErrorReporter errorReporter;
    private Exception exception = new IOException("Something went wrong...");

    @Start
    public void start(Stage stage) {
        new ErrorDialog(errorReporter, exception).show();
    }

    @Test
    @DisplayName("sends an error report when the user clicks \"report error\"")
    void sendErrorReport() {
        clickOn(".button");
        Mockito.verify(errorReporter).sendReport(exception);
    }
}