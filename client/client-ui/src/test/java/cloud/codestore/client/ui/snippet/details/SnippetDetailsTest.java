package cloud.codestore.client.ui.snippet.details;

import cloud.codestore.client.Snippet;
import cloud.codestore.client.SnippetBuilder;
import cloud.codestore.client.ui.AbstractUiTest;
import cloud.codestore.client.ui.QuickFilterEvent;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.Start;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.testfx.assertions.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("The details controller")
class SnippetDetailsTest extends AbstractUiTest {
    private SnippetDetails controller;

    @Start
    public void start(Stage stage) throws Exception {
        controller = new SnippetDetails();
        start(stage, "details.fxml", controller);
    }

    @Test
    @DisplayName("sets the editability of the details")
    void setEditable() {
        var tagsInput = tagsInput();
        var tagsQuickFilter = tagsQuickFilterPane();

        controller.setEditing(true);
        assertThat(tagsInput).isVisible();
        assertThat(tagsQuickFilter).isInvisible();

        controller.setEditing(false);
        assertThat(tagsInput).isInvisible();
        assertThat(tagsQuickFilter).isVisible();
    }

    @Test
    @DisplayName("reads the tags into the given snippet builder")
    void readTags() {
        interact(() -> tagsInput().setText("A B C"));

        SnippetBuilder builder = Snippet.builder();
        controller.visit(builder);

        assertThat(builder.build().getTags()).containsExactlyInAnyOrder("A", "B", "C");
    }

    @Nested
    @DisplayName("when receiving a snippet")
    class VisitSnippet {
        @BeforeEach
        void setUp() {
            List<String> tags = List.of("A", "B", "C");
            Snippet snippet = Snippet.builder().tags(tags).build();
            interact(() -> controller.visit(snippet));
        }

        @Test
        @DisplayName("sets the tags of the given snippet")
        void setTags() {
            assertThat(tagsInput()).hasText("A B C");
        }

        @Test
        @DisplayName("creates quickfilter nodes for all tags")
        void createTagQuickFilterNodes() {
            ObservableList<Node> tagNodes = tagsQuickFilterPane().getChildren();
            List<String> tagLabels = collectTagLabels(tagNodes);
            assertThat(tagLabels).containsExactlyInAnyOrder("A", "B", "C");
        }

        @Test
        @DisplayName("fires a QuickFilterEvent when clicking on a tag")
        void triggerTagQuickFilterEvent() {
            AtomicReference<QuickFilterEvent> capturedEvent = new AtomicReference<>();
            Pane pane = lookup("#details").queryAs(Pane.class);
            pane.addEventFilter(QuickFilterEvent.ANY, capturedEvent::set);

            Label quickFilterNode = firstQuickFilterNode();
            clickOn(quickFilterNode);

            QuickFilterEvent event = capturedEvent.get();
            assertThat(event).isNotNull();
            assertThat(event.getTag()).isPresent().get().isEqualTo(quickFilterNode.getText());
        }

        private List<String> collectTagLabels(ObservableList<Node> tagNodes) {
            return tagNodes.stream()
                           .filter(node -> node instanceof Label)
                           .map(label ->  ((Label) label).getText())
                           .toList();
        }

        private Label firstQuickFilterNode() {
            Node node = tagsQuickFilterPane().getChildren().getFirst();
            if (node instanceof Label label) {
                return label;
            } else {
                throw new RuntimeException("First label is not a label");
            }
        }
    }

    private TextInputControl tagsInput() {
        return lookup("#tagsInput").queryTextInputControl();
    }

    private Pane tagsQuickFilterPane() {
        return lookup("#quickFilterTags").query();
    }
}