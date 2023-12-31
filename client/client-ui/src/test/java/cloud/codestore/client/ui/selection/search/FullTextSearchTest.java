package cloud.codestore.client.ui.selection.search;

import cloud.codestore.client.ui.AbstractUiTest;
import com.google.common.eventbus.EventBus;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class, ApplicationExtension.class})
@DisplayName("The full-text-search input field")
class FullTextSearchTest extends AbstractUiTest {
    @Mock
    private EventBus eventBus;

    @Start
    private void start(Stage stage) throws Exception {
        FullTextSearch controller = new FullTextSearch(eventBus);
        start(stage, "searchField.fxml", controller);
    }

    @Test
    @DisplayName("triggers a FullTextSearchEvent when the input changes")
    void triggerFullTextSearch(FxRobot robot) {
        var argument = ArgumentCaptor.forClass(FullTextSearchEvent.class);

        inputField(robot).setText("test");

        verify(eventBus).post(argument.capture());
        var event = argument.getValue();
        assertThat(event.searchQuery()).isEqualTo("test");
    }

    @Test
    @DisplayName("is cleared when pressing ESC")
    void clearInputOnESC(FxRobot robot) {
        var inputField = inputField(robot);
        inputField.setText("test");
        assertThat(inputField.getText()).isNotEmpty();

        inputField.requestFocus();
        robot.press(KeyCode.ESCAPE);

        assertThat(inputField.getText()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("is cleared when pressing the search-icon")
    void clearInputIcon(FxRobot robot) {
        var inputField = inputField(robot);
        inputField.setText("test");
        assertThat(inputField.getText()).isNotEmpty();

        Labeled icon = icon(robot);
        icon.setPrefWidth(30);
        robot.clickOn(icon);

        assertThat(inputField.getText()).isNotNull().isEmpty();
    }

    private TextInputControl inputField(FxRobot robot) {
        return robot.lookup("#inputField").queryTextInputControl();
    }

    private Labeled icon(FxRobot robot) {
        return robot.lookup(".search.clearable").queryLabeled();
    }
}