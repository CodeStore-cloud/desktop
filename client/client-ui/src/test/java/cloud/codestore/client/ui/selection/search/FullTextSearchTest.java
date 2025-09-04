package cloud.codestore.client.ui.selection.search;

import cloud.codestore.client.ui.AbstractUiTest;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.testfx.assertions.api.Assertions.assertThat;

@DisplayName("The full-text-search input field")
class FullTextSearchTest extends AbstractUiTest {
    private FullTextSearch controller = new FullTextSearch();

    @Start
    public void start(Stage stage) throws Exception {
        start(stage, "searchField.fxml", controller);
    }

    @Test
    @DisplayName("is focused via the shortcut ctrl-F")
    void focusOnCtrlF() {
        var inputField = inputField();
        assertThat(inputField).isNotFocused();
        push(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
        assertThat(inputField).isFocused();
    }

    @Test
    @DisplayName("updates the Property-Object when the input changes")
    void triggerFullTextSearch() {
        assertThat(inputPropertyValue()).isEmpty();
        interact(() -> inputField().setText("test"));
        assertThat(inputPropertyValue()).isEqualTo("test");
    }

    @Test
    @DisplayName("is cleared when pressing ESC")
    void clearInputOnESC() {
        var inputField = inputField();
        interact(() -> inputField.setText("test"));
        assertThat(inputField.getText()).isEqualTo("test");

        interact(inputField::requestFocus);
        press(KeyCode.ESCAPE);

        assertThat(inputField.getText()).isNotNull().isEmpty();
        assertThat(inputPropertyValue()).isEmpty();
    }

    @Test
    @DisplayName("is cleared when pressing the search-icon")
    void clearInputIcon() {
        var inputField = inputField();
        interact(() -> inputField.setText("test"));
        assertThat(inputField.getText()).isNotEmpty();

        Labeled icon = icon();
        icon.setPrefWidth(30);
        clickOn(icon);

        assertThat(inputField.getText()).isNotNull().isEmpty();
        assertThat(inputPropertyValue()).isEmpty();
    }

    @Test
    @DisplayName("calls the corresponding key handler when a special key is pressed")
    void keyHandlers() {
        AtomicBoolean enterPressed = new AtomicBoolean(false);
        interact(() -> inputField().requestFocus());
        type(KeyCode.ENTER);
        assertThat(enterPressed).isFalse();

        controller.registerKeyHandler(KeyCode.ENTER, () -> enterPressed.set(true));
        type(KeyCode.ENTER);
        assertThat(enterPressed).isTrue();
    }

    private TextInputControl inputField() {
        return lookup("#inputField").queryTextInputControl();
    }

    private String inputPropertyValue() {
        return controller.inputProperty().get();
    }

    private Labeled icon() {
        return lookup(".search.clearable").queryLabeled();
    }
}