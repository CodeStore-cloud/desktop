package cloud.codestore.client.ui.snippet.details;

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

import java.util.List;

import static org.testfx.assertions.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("The details controller")
class SnippetDetailsTest extends AbstractUiTest {
    private SnippetDetails controller = new SnippetDetails();

    @Start
    public void start(Stage stage) throws Exception {
        start(stage, "details.fxml", controller);
    }

    @Test
    @DisplayName("sets the editability of the details")
    void setEditable() {
        var tagsInput = tagsInput();

        controller.setEditing(true);
        assertThat(tagsInput.isEditable()).isTrue();

        controller.setEditing(false);
        assertThat(tagsInput.isEditable()).isFalse();
    }

    @Test
    @DisplayName("sets the tags of the given snippet")
    void setTags() {
        Snippet snippet = new SnippetBuilder().uri("").tags(List.of("A", "B", "C")).build();
        controller.visit(snippet);
        assertThat(tagsInput()).hasText("A B C");
    }

    @Test
    @DisplayName("reads the tags into the given snippet builder")
    void readTags() {
        tagsInput().setText("A B C");

        SnippetBuilder builder = new SnippetBuilder().uri("");
        controller.visit(builder);

        assertThat(builder.build().getTags()).containsExactlyInAnyOrder("A", "B", "C");
    }

    private TextInputControl tagsInput() {
        return lookup("#tagsInput").queryTextInputControl();
    }
}