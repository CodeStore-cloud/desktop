package cloud.codestore.core.application.update;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.testfx.assertions.api.Assertions.assertThat;

@DisplayName("The update dialog")
class UpdateDialogTest extends ApplicationTest {

    private final UpdateDialog updateDialog = new UpdateDialog();

    @Start
    public void start(Stage stage) throws Exception {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("dialog-messages");
        URL fxmlFile = getClass().getResource("updateDialog.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlFile, resourceBundle);
        fxmlLoader.setControllerFactory(controllerClass -> updateDialog);
        Stage window = fxmlLoader.load();
        window.show();
    }

    @Test
    @DisplayName("shows the given progress")
    void showsProgress() {
        ProgressBar progressBar = lookup(".progress-bar").query();
        assertThat(progressBar.getProgress()).isEqualTo(0);

        updateDialog.setProgress(0.5);
        assertThat(progressBar.getProgress()).isEqualTo(0.5);

        updateDialog.setProgress(1);
        assertThat(progressBar.getProgress()).isEqualTo(1);
    }

    @Test
    @DisplayName("calls the given callback when the user clicks \"cancel\"")
    void callsCancelCallback() {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        assertThat(atomicBoolean.get()).isFalse();
        updateDialog.onCancel(() -> atomicBoolean.set(true));

        clickOn(".button");
        assertThat(atomicBoolean.get()).isTrue();
    }
}