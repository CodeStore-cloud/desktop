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
import org.testfx.framework.junit5.Start;

import static org.testfx.assertions.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("The title controller")
class SnippetTitleTest extends AbstractUiTest {
    private final SnippetTitle controller = new SnippetTitle();

    @Start
    public void start(Stage stage) throws Exception {
        start(stage, "title.fxml", controller);
    }

    @Test
    @DisplayName("sets the editability of the text field")
    void setEditable() {
        var textField = textField();

        controller.setEditing(true);
        assertThat(textField.isEditable()).isTrue();

        controller.setEditing(false);
        assertThat(textField.isEditable()).isFalse();
    }

    @Test
    @DisplayName("sets the title of the given snippet")
    void setTitle() {
        Snippet snippet = Snippet.builder().title("A simple title").build();
        controller.visit(snippet);
        assertThat(textField()).hasText(snippet.getTitle());
    }

    @Test
    @DisplayName("reads the title into the given snippet builder")
    void readTitle() {
        String title = "A title to test data collection";
        textField().setText(title);

        SnippetBuilder builder = Snippet.builder();
        controller.visit(builder);

        assertThat(builder.build().getTitle()).isEqualTo(title);
    }

    private TextInputControl textField() {
        return lookup("#snippetTitle").queryTextInputControl();
    }
}