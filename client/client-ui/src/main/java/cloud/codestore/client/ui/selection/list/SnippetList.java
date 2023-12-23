package cloud.codestore.client.ui.selection.list;

import cloud.codestore.client.ui.FxController;
import cloud.codestore.client.ui.selection.filter.FilterEvent;
import cloud.codestore.client.ui.selection.search.FullTextSearchEvent;
import cloud.codestore.client.usecases.listsnippets.FilterProperties;
import cloud.codestore.client.usecases.listsnippets.ReadSnippetsUseCase;
import cloud.codestore.client.usecases.listsnippets.SnippetListItem;
import cloud.codestore.client.usecases.listsnippets.SnippetPage;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;

import javax.annotation.Nonnull;

@FxController
public class SnippetList implements ChangeListener<SnippetListItem> {
    private final ReadSnippetsUseCase readSnippetsUseCase;
    private final EventBus eventBus;
    private final StringProperty nextPageUrl = new SimpleStringProperty();

    private String searchQuery = "";
    private FilterProperties filterProperties = new FilterProperties();

    @FXML
    private ListView<SnippetListItem> list;
    @FXML
    private Node nextPage;

    public SnippetList(@Nonnull ReadSnippetsUseCase readSnippetsUseCase, @Nonnull EventBus eventBus) {
        this.readSnippetsUseCase = readSnippetsUseCase;
        this.eventBus = eventBus;
        eventBus.register(this);
    }

    @FXML
    public void initialize() {
        handleNextPageVisibility();
        handleSnippetSelection();
        loadSnippets();
    }

    private void handleNextPageVisibility() {
        nextPage.managedProperty().bind(nextPage.visibleProperty());
        nextPage.visibleProperty().bind(nextPageUrl.isNotEmpty());
    }

    private void handleSnippetSelection() {
        list.setItems(FXCollections.observableArrayList());
        list.setCellFactory(list -> new SnippetListItemCell());
        list.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        list.getSelectionModel().selectedItemProperty().addListener(this);
    }

    private void loadSnippets() {
        list.getItems().clear();
        SnippetPage page = readSnippetsUseCase.getFirstPage(searchQuery, filterProperties);
        showSnippets(page);
    }

    private void showSnippets(SnippetPage page) {
        list.getItems().addAll(page.snippets());
        nextPageUrl.set(page.nextPageUrl());
    }

    @FXML
    public void loadNextPage() {
        SnippetPage page = readSnippetsUseCase.getPage(nextPageUrl.get());
        showSnippets(page);
    }

    @Override
    public void changed(
            ObservableValue<? extends SnippetListItem> observable,
            SnippetListItem oldSelection,
            SnippetListItem newSelection
    ) {
        if (newSelection != null) {
            eventBus.post(new SnippetSelectedEvent(newSelection.uri()));
        }
    }

    @Subscribe
    private void search(@Nonnull FullTextSearchEvent event) {
        searchQuery = event.searchQuery();
        loadSnippets();
    }

    @Subscribe
    private void filterChange(@Nonnull FilterEvent event) {
        filterProperties = event.filterProperties();
        loadSnippets();
    }
}
