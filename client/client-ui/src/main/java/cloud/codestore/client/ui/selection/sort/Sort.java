package cloud.codestore.client.ui.selection.sort;

import cloud.codestore.client.ui.FxController;
import cloud.codestore.client.ui.UiMessages;
import cloud.codestore.client.usecases.listsnippets.SortProperties;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;

import java.util.Arrays;

import static cloud.codestore.client.usecases.listsnippets.SortProperties.SnippetProperty.RELEVANCE;
import static cloud.codestore.client.usecases.listsnippets.SortProperties.SnippetProperty.TITLE;

@FxController
public class Sort {
    @FXML
    private Pane sortPanel;
    @FXML
    private ComboBox<SortItem> sortSelection;
    private ObjectProperty<SortProperties> sortProperties = new SimpleObjectProperty<>();

    @FXML
    private void initialize() {
        sortPanel.managedProperty().bind(sortPanel.visibleProperty());
        sortPanel.setVisible(false);

        fillDropdown();
        sortProperties.addListener((observable, oldValue, newValue) -> updateSelection());
        sortProperties.set(new SortProperties());
    }

    public void hide() {
        sortPanel.setVisible(false);
    }

    public void toggle() {
        sortPanel.setVisible(!sortPanel.isVisible());
    }

    public ObjectProperty<SortProperties> sortProperties() {
        return sortProperties;
    }

    private void fillDropdown() {
        var items = Arrays.stream(SortProperties.SnippetProperty.values())
                          .map(property -> {
                              String labelKey = String.format("sort.%s", property.name().toLowerCase());
                              return new SortItem(property, UiMessages.get(labelKey));
                          })
                          .toList();

        sortSelection.getItems().addAll(items);
    }

    private void updateSelection() {
        var property = this.sortProperties.get().property();
        for (SortItem item : sortSelection.getItems()) {
            if (item.property() == property) {
                sortSelection.getSelectionModel().select(item);
                break;
            }
        }
    }

    @FXML
    private void sortChanged() {
        var selectedItem = sortSelection.getSelectionModel().getSelectedItem();
        var property = selectedItem.property();
        this.sortProperties.set(new SortProperties(property, property == RELEVANCE || property == TITLE));
    }
}
