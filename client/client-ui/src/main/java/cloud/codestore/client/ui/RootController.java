package cloud.codestore.client.ui;

import cloud.codestore.client.ui.selection.SelectionController;
import cloud.codestore.client.ui.snippet.SnippetController;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

/**
 * The root controller acts as bridge between the selection component and the snippet editor.
 */
@FxController
public class RootController {
    @FXML
    private Pane root;
    @FXML
    private SelectionController selectionController;
    @FXML
    private SnippetController snippetController;

    @FXML
    private void initialize() {
        selectionController.setSelectedSnippetProperty(snippetController.selectedSnippetProperty());
        root.addEventHandler(ChangeSnippetsEvent.CREATE_SNIPPET, snippetController::createSnippet);
    }
}
