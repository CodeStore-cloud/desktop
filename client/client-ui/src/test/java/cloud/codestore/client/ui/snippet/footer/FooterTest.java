package cloud.codestore.client.ui.snippet.footer;

import cloud.codestore.client.Permission;
import cloud.codestore.client.ui.AbstractUiTest;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("The snippet footer's")
class FooterTest {
    private BooleanProperty editingProperty = new SimpleBooleanProperty(false);
    private Footer controller = new Footer();

    @BeforeEach
    void setUp() {
        controller.bindEditing(editingProperty);
    }

    @Nested
    @ExtendWith(ApplicationExtension.class)
    @DisplayName("delete button")
    class DeleteButtonTest extends AbstractUiTest {
        private FxRobot robot;
        private Button button;

        @Start
        private void start(Stage stage) throws Exception {
            start(stage, "footer.fxml", controller);
            button = deleteButton();
        }

        @Test
        @DisplayName("is visible if deletion is permitted")
        void visibleIfPermitted() {
            editingProperty.set(false);
            controller.setPermissions(Set.of(Permission.DELETE));
            assertThat(button.isVisible()).isTrue();

            controller.setPermissions(Collections.emptySet());
            assertThat(button.isVisible()).isFalse();
        }

        @Test
        @DisplayName("is visible if not editing")
        void visibleIfNotEditing() {
            editingProperty.set(false);
            assertThat(button.isVisible()).isTrue();

            editingProperty.set(true);
            assertThat(button.isVisible()).isFalse();
        }

        @Test
        @DisplayName("calls the corresponding callback when clicked")
        void deleteCallback() {
            verifyCallbackCalled(button, robot);
        }

        private Button deleteButton() {
            var deleteButton = robot.lookup("#deleteButton").queryButton();
            deleteButton.setPrefSize(30, 30);
            return deleteButton;
        }
    }

    private void verifyCallbackCalled(Button button, FxRobot robot) {
        var callback = mock(Runnable.class);
        controller.onDelete(callback);
        robot.clickOn(button);
        verify(callback).run();
    }
}