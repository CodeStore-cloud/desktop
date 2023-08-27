package cloud.codestore.client.ui.selection.list;

import cloud.codestore.client.usecases.listsnippets.ListSnippets;
import cloud.codestore.client.usecases.listsnippets.SnippetListItem;
import com.google.common.eventbus.EventBus;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;

import java.util.List;

public class SnippetList implements ChangeListener<SnippetListItem> {
    private final ListSnippets listSnippets;
    private final EventBus eventBus;

    @FXML
    private ListView<SnippetListItem> list;

    public SnippetList(ListSnippets listSnippets, EventBus eventBus) {
        this.listSnippets = listSnippets;
        this.eventBus = eventBus;
    }

    @FXML
    public void initialize() {
        handleSnippetSelection();
        loadSnippetList();
    }

    private void handleSnippetSelection() {
        list.setItems(FXCollections.observableArrayList());
        list.setCellFactory(list -> new SnippetListItemCell());
        list.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        list.getSelectionModel().selectedItemProperty().addListener(this);
    }

    private void loadSnippetList() {
        List<SnippetListItem> snippets = listSnippets.list();
        list.getItems().setAll(snippets);
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
}
