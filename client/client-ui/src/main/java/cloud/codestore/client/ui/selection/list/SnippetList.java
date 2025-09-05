package cloud.codestore.client.ui.selection.list;

import cloud.codestore.client.Permission;
import cloud.codestore.client.ui.ChangeSnippetsEvent;
import cloud.codestore.client.ui.FxController;
import cloud.codestore.client.usecases.listsnippets.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import javax.annotation.Nonnull;
import java.util.Objects;

@FxController
public class SnippetList {
    private final ReadSnippetsUseCase readSnippetsUseCase;
    private final StringProperty nextPageUrl = new SimpleStringProperty();
    private StringProperty selectedSnippet = new SimpleStringProperty("");
    private boolean handleSelectionChanges = true;

    @FXML
    private Button createSnippet;
    @FXML
    private ListView<SnippetListItem> list;
    @FXML
    private Node nextPage;

    SnippetList(@Nonnull ReadSnippetsUseCase readSnippetsUseCase) {
        this.readSnippetsUseCase = readSnippetsUseCase;
    }

    @FXML
    private void initialize() {
        createSnippet.managedProperty().bind(createSnippet.visibleProperty());
        handleNextPageVisibility();
        handleSnippetSelection();

        createSnippet.sceneProperty()
                     .addListener((observable, oldValue, scene) ->
                             scene.getAccelerators().put(
                                     new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN),
                                     this::createNewSnippet
                             ));
    }

    public void setSelectedSnippetProperty(@Nonnull StringProperty selectedSnippet) {
        this.selectedSnippet = selectedSnippet;
        selectedSnippet.addListener((observable, oldValue, newValue) ->
                runWithoutHandlingSnippetSelection(this::updateSelection));
    }

    public void update(String searchQuery, FilterProperties filterProperties, SortProperties sortProperties) {
        runWithoutHandlingSnippetSelection(() -> {
            list.getItems().clear();
            SnippetPage page = readSnippetsUseCase.getPage(searchQuery, filterProperties, sortProperties);
            createSnippet.setVisible(page.permissions().contains(Permission.CREATE));
            showSnippets(page);

            int snippetIndex = updateSelection();
            list.scrollTo(snippetIndex);
        });
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
                    // Error when selecting a new snippet while creating a new one:
                    // IndexOutOfBoundsException: [ fromIndex: 0, toIndex: 1, size: 0 ]
                    // Workaround: Platform.runLater
                    Platform.runLater(() -> selectionChanged(newValue));
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
        createSnippet.fireEvent(new ChangeSnippetsEvent(ChangeSnippetsEvent.CREATE_SNIPPET));
    }

    private void selectionChanged(SnippetListItem newSelection) {
        if (newSelection != null && !Objects.equals(selectedSnippet.get(), newSelection.uri())) {
            runWithoutHandlingSnippetSelection(() -> {
                keepCurrentSelection();
                selectedSnippet.set(newSelection.uri());
            });
        }
    }

    /**
     * Selecting another snippet is handled asynchronously.
     * So we need to reset the selected item in the list to the currently selected snippet.
     */
    private void keepCurrentSelection() {
        updateSelection();
    }

    /**
     * Sets the selected element in the list to the one defined by {@link #selectedSnippet}.
     *
     * @return the index of the snippet in the list.
     */
    private int updateSelection() {
        int index = findSelectedSnippetIndex();
        list.getSelectionModel().select(index);
        return index;
    }

    private int findSelectedSnippetIndex() {
        String selectedSnippetUri = selectedSnippet.get();
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

    /**
     * In some cases, we need to avoid recursive snippet selection.
     * This method disables handling the snippet selection while running the given action.
     */
    private void runWithoutHandlingSnippetSelection(Runnable action) {
        try {
            handleSelectionChanges = false;
            action.run();
        } finally {
            handleSelectionChanges = true;
        }
    }
}
