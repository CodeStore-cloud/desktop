package cloud.codestore.client.ui.snippet.footer;

import cloud.codestore.client.Permission;
import cloud.codestore.client.Snippet;
import cloud.codestore.client.SnippetBuilder;
import cloud.codestore.client.ui.AbstractUiTest;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.Set;

import static org.testfx.assertions.api.Assertions.assertThat;

@DisplayName("The snippet footer's")
class SnippetFooterTest {
    private SnippetFooter controller = new SnippetFooter();

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
            controller.visit(snippet(Permission.DELETE));
            assertThat(button).isVisible();

            controller.visit(snippet());
            assertThat(button).isInvisible();
        }

        @Test
        @DisplayName("is visible if not editing")
        void visibleIfNotEditing() {
            controller.setEditable(true);
            assertThat(button).isInvisible();

            controller.setEditable(false);
            assertThat(button).isVisible();
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
            controller.setEditable(true);
            assertThat(saveButton.isVisible()).isTrue();
            assertThat(cancelButton.isVisible()).isTrue();

            controller.setEditable(false);
            assertThat(saveButton.isVisible()).isFalse();
            assertThat(cancelButton.isVisible()).isFalse();
        }
    }

    private Snippet snippet(Permission... permissions) {
        return new SnippetBuilder().uri("").permissions(Set.of(permissions)).build();
    }

    private Button getButton(String buttonId, FxRobot robot) {
        var button = robot.lookup(buttonId).queryButton();
        button.setPrefSize(30, 30);
        return button;
    }
}