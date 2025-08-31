package cloud.codestore.client.ui.selection.search;

import cloud.codestore.client.ui.AbstractUiTest;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("The full-text-search input field")
class FullTextSearchTest extends AbstractUiTest {
    private FullTextSearch controller = new FullTextSearch();

    @Start
    public void start(Stage stage) throws Exception {
        start(stage, "searchField.fxml", controller);
    }

    @Test
    @DisplayName("triggers a FullTextSearchEvent when the input changes")
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