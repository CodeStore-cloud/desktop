package cloud.codestore.core.application.update;

import javafx.stage.Stage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;

import java.io.IOException;
import java.lang.reflect.Field;

@DisplayName("The error dialog")
class ErrorDialogTest extends ApplicationTest {
    private Exception exception = new IOException("Something went wrong...");

    @Start
    public void start(Stage stage) throws Exception {
        ErrorDialog errorDialog = new ErrorDialog(exception);
        AbstractDialog.UI delegate = new AbstractDialog.UI();

        Field field = AbstractDialog.UI.class.getDeclaredField("controller");
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, delegate, errorDialog);

        delegate.start(stage);
    }

    @Test
    @DisplayName("sends an error report when the user clicks \"report error\"")
    void sendErrorReport() {
        clickOn(".button");
        Assertions.fail();
    }
}