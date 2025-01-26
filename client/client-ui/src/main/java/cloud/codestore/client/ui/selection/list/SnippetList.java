package cloud.codestore.client.ui.selection.list;

import cloud.codestore.client.Permission;
import cloud.codestore.client.ui.FxController;
import cloud.codestore.client.ui.selection.filter.FilterEvent;
import cloud.codestore.client.ui.selection.search.FullTextSearchEvent;
import cloud.codestore.client.ui.selection.sort.SortEvent;
import cloud.codestore.client.ui.snippet.SnippetCreatedEvent;
import cloud.codestore.client.ui.snippet.SnippetDeletedEvent;
import cloud.codestore.client.ui.snippet.SnippetUpdatedEvent;
import cloud.codestore.client.usecases.listsnippets.*;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;

import javax.annotation.Nonnull;
import java.util.Objects;

@FxController
public class SnippetList implements ChangeListener<SnippetListItem> {
    private final ReadSnippetsUseCase readSnippetsUseCase;
    private final EventBus eventBus;
    private final StringProperty nextPageUrl = new SimpleStringProperty();

    private String currentSnippetUri = "";
    private String searchQuery = "";
    private FilterProperties filterProperties = new FilterProperties();
    private SortProperties sortProperties = new SortProperties();

    @FXML
    private Button createSnippet;
    @FXML
    private ListView<SnippetListItem> list;
    @FXML
    private Node nextPage;

    SnippetList(@Nonnull ReadSnippetsUseCase readSnippetsUseCase, @Nonnull EventBus eventBus) {
        this.readSnippetsUseCase = readSnippetsUseCase;
        this.eventBus = eventBus;
        eventBus.register(this);
    }

    @FXML
    public void initialize() {
        createSnippet.managedProperty().bind(createSnippet.visibleProperty());
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
        SnippetPage page = readSnippetsUseCase.getPage(searchQuery, filterProperties, sortProperties);
        createSnippet.setVisible(page.permissions().contains(Permission.CREATE));
        showSnippets(page);
        updateSelection();
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
        if (newSelection != null && !Objects.equals(currentSnippetUri, newSelection.uri())) {
            currentSnippetUri = newSelection.uri();
            eventBus.post(new SnippetSelectedEvent(newSelection.uri()));
        }
    }

    @FXML
    public void createNewSnippet() {
        eventBus.post(new CreateSnippetEvent());
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

    @Subscribe
    private void sortChange(@Nonnull SortEvent event) {
        sortProperties = event.sortProperties();
        loadSnippets();
    }

    @Subscribe
    private void snippetSelected(@Nonnull SnippetSelectedEvent event) {
        if (!Objects.equals(currentSnippetUri, event.snippetUri())) {
            currentSnippetUri = event.snippetUri();
            updateSelection();
        }
    }

    @Subscribe
    private void snippetCreated(@Nonnull SnippetCreatedEvent event) {
        currentSnippetUri = event.snippetUri();
        loadSnippets();
    }

    @Subscribe
    private void snippetUpdated(@Nonnull SnippetUpdatedEvent event) {
        loadSnippets();
    }

    @Subscribe
    private void snippetDeleted(@Nonnull SnippetDeletedEvent event) {
        loadSnippets();
    }

    private void updateSelection() {
        if (!currentSnippetUri.isEmpty()) {
            int currentSnippetIndex = findSelectedSnippetIndex();
            if (currentSnippetIndex >= 0) {
                list.getSelectionModel().select(currentSnippetIndex);
            }
        }
    }

    private int findSelectedSnippetIndex() {
        ObservableList<SnippetListItem> listItems = list.getItems();
        for (int i = 0; i < listItems.size(); i++) {
            SnippetListItem item = listItems.get(i);
            if (Objects.equals(item.uri(), currentSnippetUri)) {
                return i;
            }
        }

        return -1;
    }
}
