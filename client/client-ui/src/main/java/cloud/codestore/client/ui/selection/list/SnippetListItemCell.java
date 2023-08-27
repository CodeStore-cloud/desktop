package cloud.codestore.client.ui.selection.list;

import cloud.codestore.client.usecases.listsnippets.SnippetListItem;
import javafx.scene.control.ListCell;

/**
 * Represents a single cell in the snippet list.
 */
class SnippetListItemCell extends ListCell<SnippetListItem> {
    @Override
    public void updateItem(SnippetListItem item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
        } else {
            setText(item.title());
        }
    }
}
