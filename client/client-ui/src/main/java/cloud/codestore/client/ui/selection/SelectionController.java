package cloud.codestore.client.ui.selection;

import cloud.codestore.client.ui.ApplicationReadyEvent;
import cloud.codestore.client.ui.FxController;
import cloud.codestore.client.ui.selection.filter.Filter;
import cloud.codestore.client.ui.selection.history.History;
import cloud.codestore.client.ui.selection.list.SnippetList;
import cloud.codestore.client.ui.selection.search.FullTextSearch;
import cloud.codestore.client.ui.selection.sort.Sort;
import cloud.codestore.client.ui.snippet.SnippetCreatedEvent;
import cloud.codestore.client.ui.snippet.SnippetDeletedEvent;
import cloud.codestore.client.ui.snippet.SnippetUpdatedEvent;
import cloud.codestore.client.usecases.listsnippets.FilterProperties;
import cloud.codestore.client.usecases.listsnippets.SortProperties;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;

import javax.annotation.Nonnull;

import static cloud.codestore.client.usecases.listsnippets.SortProperties.SnippetProperty.RELEVANCE;

/**
 * The top most controller for handling the interaction between search, filter and sort components.
 * This controller also handles the visibility of the sort and filter components.
 */
@FxController
public class SelectionController {
    private static final String FILLED_FILTER_BUTTON_STYLE = "filled";

    @FXML
    private Button filterButton;
    @FXML
    private FullTextSearch searchController;
    @FXML
    private Filter filterController;
    @FXML
    private Sort sortController;
    @FXML
    private SnippetList snippetListController;
    private History historyController;

    private StringProperty searchInputProperty;
    private ObjectProperty<SortProperties> sortProperties;
    private SortProperties previousSortProperties = new SortProperties();
    private ObjectProperty<FilterProperties> filterProperties;
    private boolean updateSnippetListEnabled = true;

    public SelectionController(@Nonnull EventBus eventBus, @Nonnull History historyController) {
        eventBus.register(this);
        this.historyController = historyController;
    }

    public void setSelectedSnippetProperty(@Nonnull StringProperty selectedSnippetProperty) {
        snippetListController.setSelectedSnippetProperty(selectedSnippetProperty);
        historyController.setSelectedSnippetProperty(selectedSnippetProperty);
    }

    @FXML
    private void initialize() {
        searchInputProperty = searchController.inputProperty();
        searchInputProperty.addListener((textField, oldValue, newValue) -> onSearch());

        filterProperties = filterController.filterProperties();
        filterProperties.addListener(((observable, oldValue, newValue) -> onFilter()));

        sortProperties = sortController.sortProperties();
        sortProperties.addListener(((observable, oldValue, newValue) -> onSort()));

        registerControlKeyHandlers();
    }

    private void registerControlKeyHandlers() {
        searchController.registerKeyHandler(KeyCode.DOWN, snippetListController::selectNextSnippet);
        searchController.registerKeyHandler(KeyCode.UP, snippetListController::selectPreviousSnippet);
    }

    @FXML
    private void toggleFilter() {
        sortController.hide();
        filterController.toggle();
    }

    @FXML
    private void toggleSort() {
        filterController.hide();
        sortController.toggle();
    }

    /**
     * When the user enters a search term, the sorting is changed to relevance.
     * When the search is cleared, the sorting is reset to the previous selection.
     */
    private void onSearch() {
        try {
            // Changing the sortProperties results in onSort() being called.
            // So we need to ensure that updating the snippet list is only called once.
            updateSnippetListEnabled = false;
            if (searchInputProperty.get().isEmpty()) {
                sortProperties.set(previousSortProperties);
            } else {
                sortProperties.set(new SortProperties(RELEVANCE, true));
            }
        } finally {
            updateSnippetListEnabled = true;
        }

        updateSnippetList();
    }

    /**
     * Updates the style filter-button depending on whether there are active filters or not.
     */
    private void onFilter() {
        ObservableList<String> classes = filterButton.getStyleClass();
        if (filterProperties.get().isEmpty()) {
            classes.remove(FILLED_FILTER_BUTTON_STYLE);
        } else if (!classes.contains(FILLED_FILTER_BUTTON_STYLE)) {
            classes.add(FILLED_FILTER_BUTTON_STYLE);
        }

        updateSnippetList();
    }

    private void onSort() {
        SortProperties sortProperties = this.sortProperties.get();
        if (sortProperties.property() != RELEVANCE) {
            previousSortProperties = sortProperties;
        }

        updateSnippetList();
    }

    @Subscribe
    private void snippetCreated(@Nonnull SnippetCreatedEvent event) {
        updateSnippetList();
    }

    @Subscribe
    private void snippetUpdated(@Nonnull SnippetUpdatedEvent event) {
        updateSnippetList();
    }

    @Subscribe
    private void snippetDeleted(@Nonnull SnippetDeletedEvent event) {
        updateSnippetList();
    }

    @Subscribe
    private void applicationReady(@Nonnull ApplicationReadyEvent event) {
        updateSnippetList();
    }

    private void updateSnippetList() {
        if (updateSnippetListEnabled) {
            snippetListController.update(searchInputProperty.get(), filterProperties.get(), sortProperties.get());
        }
    }
}
