package cloud.codestore.client.ui.snippet.title;


import cloud.codestore.client.Snippet;
import cloud.codestore.client.SnippetBuilder;
import cloud.codestore.client.ui.AbstractUiTest;
import javafx.scene.control.TextInputControl;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.testfx.assertions.api.Assertions.assertThat;

@ExtendWith({MockitoExtension.class, ApplicationExtension.class})
@DisplayName("The title controller")
class SnippetTitleTest extends AbstractUiTest {
    private final SnippetTitle controller = new SnippetTitle();

    @Start
    private void start(Stage stage) throws Exception {
        start(stage, "title.fxml", controller);
    }

    @Test
    @DisplayName("sets the editability of the text field")
    void setEditable(FxRobot robot) {
        var textField = textField(robot);

        controller.setEditable(true);
        assertThat(textField.isEditable()).isTrue();

        controller.setEditable(false);
        assertThat(textField.isEditable()).isFalse();
    }

    @Test
    @DisplayName("sets the title of the given snippet")
    void setTitle(FxRobot robot) {
        Snippet snippet = new SnippetBuilder().uri("").title("A simple title").build();
        controller.visit(snippet);
        assertThat(textField(robot)).hasText(snippet.getTitle());
    }

    @Test
    @DisplayName("reads the title into the given snippet builder")
    void readTitle(FxRobot robot) {
        String title = "A title to test data collection";
        textField(robot).setText(title);

        SnippetBuilder builder = new SnippetBuilder().uri("");
        controller.visit(builder);

        assertThat(builder.build().getTitle()).isEqualTo(title);
    }

    private TextInputControl textField(FxRobot robot) {
        return robot.lookup("#snippetTitle").queryTextInputControl();
    }
}