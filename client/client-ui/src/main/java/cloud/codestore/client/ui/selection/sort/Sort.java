package cloud.codestore.client.ui.selection.sort;

import cloud.codestore.client.ui.FxController;
import cloud.codestore.client.ui.UiMessages;
import cloud.codestore.client.ui.selection.filter.ToggleFilterEvent;
import cloud.codestore.client.ui.selection.search.FullTextSearchEvent;
import cloud.codestore.client.usecases.listsnippets.SortProperties;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;

import javax.annotation.Nonnull;
import java.util.Arrays;

import static cloud.codestore.client.usecases.listsnippets.SortProperties.SnippetProperty;
import static cloud.codestore.client.usecases.listsnippets.SortProperties.SnippetProperty.*;

@FxController
public class Sort {
    private static final SnippetProperty DEFAULT_SORT_PROPERTY = new SortProperties().property();

    @FXML
    private Pane sortPanel;
    @FXML
    private ComboBox<SortItem> sortSelection;

    private final EventBus eventBus;
    private SortProperties.SnippetProperty previousSelection = DEFAULT_SORT_PROPERTY;

    public Sort(EventBus eventBus) {
        this.eventBus = eventBus;
        eventBus.register(this);
    }

    @FXML
    private void initialize() {
        sortPanel.managedProperty().bind(sortPanel.visibleProperty());
        sortPanel.setVisible(false);

        fillDropdown();
        select(DEFAULT_SORT_PROPERTY);
    }

    @Subscribe
    private void search(@Nonnull FullTextSearchEvent event) {
        select(event.searchQuery().isEmpty() ? previousSelection : RELEVANCE);
        triggerSortEvent();
    }

    @Subscribe
    private void toggle(@Nonnull ToggleSortEvent event) {
        sortPanel.setVisible(!sortPanel.isVisible());
    }

    @Subscribe
    private void toggle(@Nonnull ToggleFilterEvent event) {
        sortPanel.setVisible(false);
    }

    private void fillDropdown() {
        var items = Arrays.stream(values())
                          .map(property -> {
                              String labelKey = String.format("sort.%s", property.name().toLowerCase());
                              return new SortItem(property, UiMessages.get(labelKey));
                          })
                          .toList();

        sortSelection.getItems().addAll(items);
    }

    private void select(SnippetProperty property) {
        for (SortItem item : sortSelection.getItems()) {
            if (item.property() == property) {
                sortSelection.getSelectionModel().select(item);
                break;
            }
        }
    }

    @FXML
    private void triggerSortEvent() {
        var selectedItem = sortSelection.getSelectionModel().getSelectedItem();
        var property = selectedItem.property();
        var sortProperties = new SortProperties(property, property == RELEVANCE || property == TITLE);
        eventBus.post(new SortEvent(sortProperties));

        if (property != RELEVANCE) {
            previousSelection = property;
        }
    }
}
