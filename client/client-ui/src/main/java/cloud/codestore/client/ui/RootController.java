package cloud.codestore.client.ui;

import cloud.codestore.client.ui.selection.SelectionController;
import cloud.codestore.client.ui.snippet.SnippetController;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

import javax.annotation.Nonnull;

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

    RootController(@Nonnull EventBus eventBus) {
        eventBus.register(this);
    }

    @FXML
    private void initialize() {
        selectionController.setSelectedSnippetProperty(snippetController.selectedSnippetProperty());
        root.addEventHandler(ChangeSnippetsEvent.CREATE_SNIPPET, event -> snippetController.createSnippet());
        root.addEventHandler(SnippetsChangedEvent.ANY, event -> selectionController.reloadSnippets());
        root.addEventHandler(QuickFilterEvent.ANY, selectionController::addFilter);
    }

    @Subscribe
    private void applicationReady(@Nonnull ApplicationReadyEvent event) {
        selectionController.reloadSnippets();
    }
}
