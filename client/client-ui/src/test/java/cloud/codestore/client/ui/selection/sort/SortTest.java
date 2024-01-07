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
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static cloud.codestore.client.usecases.listsnippets.SortProperties.SnippetProperty.*;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class, ApplicationExtension.class})
@DisplayName("The sort controller")
class SortTest extends AbstractUiTest {
    @Spy
    private EventBus eventBus = new EventBus();
    private Sort controller;
    private FxRobot robot;
    private ComboBox<SortItem> comboBox;

    @Start
    private void start(Stage stage) throws Exception {
        controller = new Sort(eventBus);
        start(stage, "sort.fxml", controller);
        comboBox = robot.lookup("#sortSelection").queryComboBox();
    }

    @Test
    @DisplayName("selects creation time by default")
    void defaultSelection() {
        assertSelected(CREATED);
    }

    @Test
    @DisplayName("selects relevance when a search term is entered")
    void relevanceSelection() {
        robot.interact(() -> eventBus.post(new FullTextSearchEvent("test")));
        assertSelected(RELEVANCE);
    }

    @Test
    @DisplayName("selects creation time when the search is cleared")
    void defaultNoSearch() {
        robot.interact(() -> {
            eventBus.post(new FullTextSearchEvent("test"));
            eventBus.post(new FullTextSearchEvent(""));
        });
        assertSelected(CREATED);
    }

    @Test
    @DisplayName("triggers a SortEvent if the sort selection was changed")
    void triggerEventOnSelection() {
        robot.interact(() -> select(TITLE));
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