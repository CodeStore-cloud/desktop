package cloud.codestore.client.ui.selection.list;

import cloud.codestore.client.ui.FxController;
import cloud.codestore.client.usecases.listsnippets.ListSnippets;
import cloud.codestore.client.usecases.listsnippets.SnippetListItem;
import cloud.codestore.client.usecases.listsnippets.SnippetPage;
import com.google.common.eventbus.EventBus;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;

@FxController
public class SnippetList implements ChangeListener<SnippetListItem> {
    private final ListSnippets listSnippets;
    private final EventBus eventBus;
    private final StringProperty nextPageUrl = new SimpleStringProperty();

    @FXML
    private ListView<SnippetListItem> list;
    @FXML
    private Node nextPage;

    public SnippetList(ListSnippets listSnippets, EventBus eventBus) {
        this.listSnippets = listSnippets;
        this.eventBus = eventBus;
    }

    @FXML
    public void initialize() {
        handleNextPageVisibility();
        handleSnippetSelection();
        showSnippets(listSnippets.readSnippets());
    }

    @FXML
    public void loadNextPage() {
        SnippetPage page = listSnippets.readSnippets(nextPageUrl.get());
        showSnippets(page);
    }

    private void handleSnippetSelection() {
        list.setItems(FXCollections.observableArrayList());
        list.setCellFactory(list -> new SnippetListItemCell());
        list.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        list.getSelectionModel().selectedItemProperty().addListener(this);
    }

    private void showSnippets(SnippetPage page) {
        list.getItems().addAll(page.snippets());
        nextPageUrl.set(page.nextPageUrl());
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

    private void handleNextPageVisibility() {
        nextPage.managedProperty().bind(nextPage.visibleProperty());
        nextPage.visibleProperty().bind(nextPageUrl.isNotEmpty());
    }
}
