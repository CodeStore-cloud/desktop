package cloud.codestore.client.ui.selection.sort;

import cloud.codestore.client.ui.AbstractUiTest;
import cloud.codestore.client.ui.selection.search.FullTextSearchEvent;
import cloud.codestore.client.usecases.listsnippets.SortProperties;
import com.google.common.eventbus.EventBus;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.Start;

import static cloud.codestore.client.usecases.listsnippets.SortProperties.SnippetProperty.RELEVANCE;
import static cloud.codestore.client.usecases.listsnippets.SortProperties.SnippetProperty.TITLE;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("The sort controller")
class SortTest extends AbstractUiTest {
    @Spy
    private EventBus eventBus = new EventBus();
    private ComboBox<SortItem> comboBox;

    @Start
    public void start(Stage stage) throws Exception {
        start(stage, "sort.fxml", new Sort(eventBus));
        comboBox = lookup("#sortSelection").queryComboBox();
    }

    @Test
    @DisplayName("selects default option on initialization")
    void defaultSelection() {
        var defaultProperty = new SortProperties().property();
        assertSelected(defaultProperty);
    }

    @Test
    @DisplayName("selects relevance when a search term is entered")
    void relevanceSelection() {
        interact(() -> eventBus.post(new FullTextSearchEvent("test")));
        assertSelected(RELEVANCE);
    }

    @Test
    @DisplayName("selects previous option when the search is cleared")
    void defaultNoSearch() {
        interact(() -> {
            select(TITLE);
            eventBus.post(new FullTextSearchEvent("test"));
            assertSelected(RELEVANCE);
            eventBus.post(new FullTextSearchEvent(""));
            assertSelected(TITLE);
        });
    }

    @Test
    @DisplayName("triggers a SortEvent if the sort selection was changed")
    void triggerEventOnSelection() {
        interact(() -> select(TITLE));
        var expectedEvent = new SortEvent(new SortProperties(TITLE, true));
        verify(eventBus).post(expectedEvent);
    }

    private void select(SortProperties.SnippetProperty property) {
        for (SortItem item : comboBox.getItems()) {
            if (item.property() == property) {
                comboBox.getSelectionModel().select(item);
                break;
            }
        }
    }

    private void assertSelected(SortProperties.SnippetProperty property) {
        SortItem selectedItem = comboBox.getSelectionModel().getSelectedItem();
        Assertions.assertThat(selectedItem.property()).isEqualTo(property);
    }
}