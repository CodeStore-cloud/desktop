package cloud.codestore.client.ui;

import cloud.codestore.client.ui.selection.SelectionController;
import cloud.codestore.client.ui.snippet.SnippetController;
import javafx.fxml.FXML;

/**
 * The root controller acts as bridge between the selection component and the snippet editor.
 */
@FxController
public class RootController {
    @FXML
    private SelectionController selectionController;
    @FXML
    private SnippetController snippetController;

    @FXML
    private void initialize() {
        selectionController.setSelectedSnippetProperty(snippetController.selectedSnippetProperty());
    }
}
