package cloud.codestore.core.application.update;

import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.testfx.assertions.api.Assertions.assertThat;

@DisplayName("The update dialog")
class UpdateDialogTest extends ApplicationTest {

    private UpdateDialog updateDialog = new UpdateDialog();

    @Start
    public void start(Stage stage) throws Exception {
        Field field = AbstractDialog.class.getDeclaredField("window");
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, updateDialog, stage);

        updateDialog.show();
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