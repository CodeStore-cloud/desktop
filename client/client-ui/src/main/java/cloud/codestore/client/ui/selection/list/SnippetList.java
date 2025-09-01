package cloud.codestore.client.ui.selection.list;

import cloud.codestore.client.Permission;
import cloud.codestore.client.ui.FxController;
import cloud.codestore.client.usecases.listsnippets.*;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
public class SnippetList {
    private final ReadSnippetsUseCase readSnippetsUseCase;
    private final EventBus eventBus;
    private final StringProperty nextPageUrl = new SimpleStringProperty();
    private String selectedSnippetUri = "";
    private boolean handleSelectionChanges = true;

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
    private void initialize() {
        createSnippet.managedProperty().bind(createSnippet.visibleProperty());
        handleNextPageVisibility();
        handleSnippetSelection();
    }

    public void update(String searchQuery, FilterProperties filterProperties, SortProperties sortProperties) {
        try {
            handleSelectionChanges = false;
            list.getItems().clear();
            SnippetPage page = readSnippetsUseCase.getPage(searchQuery, filterProperties, sortProperties);
            createSnippet.setVisible(page.permissions().contains(Permission.CREATE));
            showSnippets(page);

            int snippetIndex = updateSelection();
            list.scrollTo(snippetIndex);
        } finally {
            handleSelectionChanges = true;
        }
    }

    public void selectNextSnippet() {
        int nextSnippetIndex = findSelectedSnippetIndex() + 1;
        if (nextSnippetIndex < list.getItems().size()) {
            list.getSelectionModel().select(nextSnippetIndex);
        }
    }

    public void selectPreviousSnippet() {
        int previousSnippetIndex = findSelectedSnippetIndex() - 1;
        if (previousSnippetIndex >= 0) {
            list.getSelectionModel().select(previousSnippetIndex);
        }
    }

    private void handleNextPageVisibility() {
        nextPage.managedProperty().bind(nextPage.visibleProperty());
        nextPage.visibleProperty().bind(nextPageUrl.isNotEmpty());
    }

    private void handleSnippetSelection() {
        list.setItems(FXCollections.observableArrayList());
        list.setCellFactory(list -> new SnippetListItemCell());
        list.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        list.getSelectionModel()
            .selectedItemProperty()
            .addListener((observable, oldValue, newValue) -> {
                if (handleSelectionChanges) {
                    selectionChanged(newValue);
                }
            });
    }

    private void showSnippets(SnippetPage page) {
        list.getItems().addAll(page.snippets());
        nextPageUrl.set(page.nextPageUrl());
    }

    @FXML
    private void loadNextPage() {
        SnippetPage page = readSnippetsUseCase.getPage(nextPageUrl.get());
        showSnippets(page);
    }

    @FXML
    private void createNewSnippet() {
        eventBus.post(new CreateSnippetEvent());
    }

    private void selectionChanged(SnippetListItem newSelection) {
        if (newSelection == null) {
            selectedSnippetUri = "";
        } else if (!Objects.equals(selectedSnippetUri, newSelection.uri())) {
            updateSelection();
            eventBus.post(new RequestSnippetSelectionEvent(newSelection.uri()));
        }
    }

    @Subscribe
    private void snippetSelected(@Nonnull SnippetSelectedEvent event) {
        if (!Objects.equals(selectedSnippetUri, event.snippetUri())) {
            selectedSnippetUri = event.snippetUri();
            updateSelection();
        }
    }

    /**
     * Sets the selected element in the list to the one defined by {@code selectedSnippetUri}.
     * @return the index of the snippet in the list.
     */
    private int updateSelection() {
        int index = findSelectedSnippetIndex();
        list.getSelectionModel().select(index);
        return index;
    }

    private int findSelectedSnippetIndex() {
        if (!selectedSnippetUri.isEmpty()) {
            ObservableList<SnippetListItem> listItems = list.getItems();
            for (int i = 0; i < listItems.size(); i++) {
                SnippetListItem item = listItems.get(i);
                if (Objects.equals(item.uri(), selectedSnippetUri)) {
                    return i;
                }
            }
        }

        return -1;
    }
}
