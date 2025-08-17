package cloud.codestore.client.ui.snippet;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.testfx.assertions.api.Assertions.assertThat;

@DisplayName("The confirmation dialog")
class ConfirmationDialogTest extends ApplicationTest {
    private ConfirmationDialog dialog;

    @Start
    public void start(Stage stage) throws Exception {
        dialog = new ConfirmationDialog("test title", "test message");
    }

    @Test
    @DisplayName("is cancellable by default")
    void cancellableByDefault() {
        showDialog();
        assertThat(cancelButton()).isVisible();
    }

    @Test
    @DisplayName("calls specific function when the user clicks \"yes\"")
    void onYes() {
        AtomicBoolean yesClicked = new AtomicBoolean(false);
        dialog.onYes(() -> yesClicked.set(true));
        showDialog();

        clickOn(yesButton());

        assertThat(yesClicked.get()).isTrue();
    }

    @Test
    @DisplayName("calls specific function when the user clicks \"no\"")
    void onNo() {
        AtomicBoolean noClicked = new AtomicBoolean(false);
        dialog.onNo(() -> noClicked.set(true));
        showDialog();

        clickOn(noButton());

        assertThat(noClicked.get()).isTrue();
    }

    @Nested
    @DisplayName("when cancellable")
    class CancellableTest {
        @BeforeEach
        void setUp() {
            dialog.setCancellable(true);
            showDialog();
        }

        @Test
        @DisplayName("shows the \"cancel\" button")
        void showsCancelButton() {
            assertThat(cancelButton()).isVisible();
        }

        @Test
        @DisplayName("sets the \"cancel\" button as cancel-button")
        void cancelButtonIsCancelButton() {
            assertThat(noButton()).isNotCancelButton();
            assertThat(cancelButton()).isCancelButton();
        }
    }

    @Nested
    @DisplayName("when not cancellable")
    class NotCancellableTest {
        @BeforeEach
        void setUp() {
            dialog.setCancellable(false);
            showDialog();
        }

        @Test
        @DisplayName("doesn't show the \"cancel\" button")
        void showsCancelButton() {
            assertThat(cancelButton()).isInvisible();
        }

        @Test
        @DisplayName("sets the \"no\" button as cancel-button")
        void noButtonIsCancelButton() {
            assertThat(noButton()).isCancelButton();
            assertThat(cancelButton()).isNotCancelButton();
        }
    }

    private void showDialog() {
        Platform.runLater(dialog::show);
        WaitForAsyncUtils.waitForFxEvents();
        assertThat(dialogWindow()).isShowing();
    }

    private Button yesButton() {
        return lookup("#yes").queryButton();
    }

    private Button noButton() {
        return lookup("#no").queryButton();
    }

    private Button cancelButton() {
        return lookup("#cancel").queryButton();
    }

    private Stage dialogWindow() {
        try {
            Field field = ConfirmationDialog.class.getDeclaredField("window");
            field.setAccessible(true);
            return (Stage) field.get(dialog);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}