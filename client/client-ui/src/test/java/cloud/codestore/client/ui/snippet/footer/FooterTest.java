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

@DisplayName("The snippet footer's")
class FooterTest {
    private Footer controller = new Footer();
    private BooleanProperty editingProperty = new SimpleBooleanProperty(false);

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
            button = getButton("#deleteButton", robot);
        }

        @Test
        @DisplayName("is visible if deletion is permitted")
        void visibleIfPermitted() {
            controller.setPermissions(Set.of(Permission.DELETE));
            assertThat(button.isVisible()).isTrue();

            controller.setPermissions(Collections.emptySet());
            assertThat(button.isVisible()).isFalse();
        }

        @Test
        @DisplayName("is visible if not editing")
        void visibleIfNotEditing() {
            assertThat(button.isVisible()).isTrue();
            editingProperty.set(true);
            assertThat(button.isVisible()).isFalse();
        }
    }

    @Nested
    @ExtendWith(ApplicationExtension.class)
    @DisplayName("save and cancel button")
    class SaveButtonTest extends AbstractUiTest {
        private FxRobot robot;
        private Button saveButton;
        private Button cancelButton;

        @Start
        private void start(Stage stage) throws Exception {
            start(stage, "footer.fxml", controller);
            saveButton = getButton("#saveButton", robot);
            cancelButton = getButton("#cancelButton", robot);
        }

        @Test
        @DisplayName("are only visible if editing")
        void visibleIfNotEditing() {
            assertThat(saveButton.isVisible()).isFalse();
            assertThat(cancelButton.isVisible()).isFalse();
            editingProperty.set(true);
            assertThat(saveButton.isVisible()).isTrue();
            assertThat(cancelButton.isVisible()).isTrue();
        }
    }

    private Button getButton(String buttonId, FxRobot robot) {
        var button = robot.lookup(buttonId).queryButton();
        button.setPrefSize(30, 30);
        return button;
    }
}