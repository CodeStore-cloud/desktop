package cloud.codestore.client.ui.snippet.footer;

import cloud.codestore.client.Permission;
import cloud.codestore.client.Snippet;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.testfx.assertions.api.Assertions.assertThat;

@DisplayName("The snippet footer's")
class SnippetFooterTest extends ApplicationTest {
    private SnippetFooter controller = new SnippetFooter();
    private Button button;

    @Start
    public void start(Stage stage) throws Exception {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("uiMessages");
        URL fxmlFile = getClass().getResource("footer.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlFile, resourceBundle);
        fxmlLoader.setControllerFactory(controllerClass -> controller);
        Parent parent = fxmlLoader.load();

        stage.setScene(new Scene(parent));
        stage.show();
    }

    @Nested
    @DisplayName("edit button")
    class EditButtonTest {
        @BeforeEach
        void setUp() {
            button = getButton("#editButton");
        }

        @Test
        @DisplayName("is visible if editing is permitted")
        void visibleIfPermitted() {
            assertVisibleIfPermitted(button, Permission.UPDATE);
        }

        @Test
        @DisplayName("is visible if not editing")
        void visibleIfNotEditing() {
            controller.visit(snippet(Permission.UPDATE));
            assertVisibleIfNotEditing(button);
        }

        @Test
        @DisplayName("fires on CTRL+E")
        void shortcut() {
            assertButtonFiresOnShortcut(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN));
        }
    }

    @Nested
    @DisplayName("delete button")
    class DeleteButtonTest {
        @BeforeEach
        void setUp() {
            button = getButton("#deleteButton");
        }

        @Test
        @DisplayName("is visible if deletion is permitted")
        void visibleIfPermitted() {
            assertVisibleIfPermitted(button, Permission.DELETE);
        }

        @Test
        @DisplayName("is visible if not editing")
        void visibleIfNotEditing() {
            controller.visit(snippet(Permission.DELETE));
            assertVisibleIfNotEditing(button);
        }

        @Test
        @DisplayName("fires on CTRL+D")
        void shortcut() {
            assertButtonFiresOnShortcut(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
        }
    }

    @Nested
    @DisplayName("save button")
    class SaveButtonTest {
        @BeforeEach
        void setUp() {
            button = getButton("#saveButton");
        }

        @Test
        @DisplayName("are only visible if editing")
        void visibleIfNotEditing() {
            assertVisibleIfEditing(button);
        }

        @Test
        @DisplayName("fires on CTRL+S")
        void shortcut() {
            assertButtonFiresOnShortcut(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        }
    }

    @Nested
    @DisplayName("cancel button")
    class CancelButtonTest {
        @BeforeEach
        void setUp() {
            button = getButton("#cancelButton");
        }

        @Test
        @DisplayName("are only visible if editing")
        void visibleIfNotEditing() {
            assertVisibleIfEditing(button);
        }

        @Test
        @DisplayName("fires on ESC")
        void shortcut() {
            assertButtonFiresOnShortcut(new KeyCodeCombination(KeyCode.ESCAPE));
        }
    }

    private Snippet snippet(Permission... permissions) {
        return Snippet.builder().permissions(Set.of(permissions)).build();
    }

    private Button getButton(String buttonId) {
        var button = lookup(buttonId).queryButton();
        button.setPrefSize(30, 30);
        return button;
    }

    void assertVisibleIfPermitted(Button button, Permission permission) {
        controller.visit(snippet(permission));
        controller.setEditing(false);
        assertThat(button).isVisible();

        controller.visit(snippet());
        controller.setEditing(false);
        assertThat(button).isInvisible();
    }

    void assertVisibleIfEditing(Button button) {
        controller.setEditing(true);
        assertThat(button).isVisible();

        controller.setEditing(false);
        assertThat(button).isInvisible();
    }

    void assertVisibleIfNotEditing(Button button) {
        controller.setEditing(true);
        assertThat(button).isInvisible();

        controller.setEditing(false);
        assertThat(button).isVisible();
    }

    void assertButtonFiresOnShortcut(KeyCodeCombination shortcut) {
        AtomicBoolean buttonFired = new AtomicBoolean(false);
        button.addEventHandler(ActionEvent.ACTION, event -> buttonFired.set(true));
        push(shortcut);
        assertThat(buttonFired.get()).isTrue();
    }
}