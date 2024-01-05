package cloud.codestore.client.ui.snippet.description;

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
@DisplayName("The description controller")
class SnippetDescriptionTest extends AbstractUiTest {
    private final SnippetDescription controller = new SnippetDescription();

    @Start
    private void start(Stage stage) throws Exception {
        start(stage, "description.fxml", controller);
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
    @DisplayName("sets the description of the given snippet")
    void setDescription(FxRobot robot) {
        Snippet snippet = new SnippetBuilder().uri("").description("A short description").build();
        controller.visit(snippet);
        assertThat(textField(robot)).hasText(snippet.getDescription());
    }

    @Test
    @DisplayName("reads the description into the given snippet builder")
    void readDescription(FxRobot robot) {
        String description = "A title to test data collection";
        textField(robot).setText(description);

        SnippetBuilder builder = new SnippetBuilder().uri("");
        controller.visit(builder);

        assertThat(builder.build().getDescription()).isEqualTo(description);
    }

    private TextInputControl textField(FxRobot robot) {
        return robot.lookup("#snippetDescription").queryTextInputControl();
    }
}