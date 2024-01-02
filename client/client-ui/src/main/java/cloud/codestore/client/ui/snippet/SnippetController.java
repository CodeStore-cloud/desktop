package cloud.codestore.client.ui.snippet;

import cloud.codestore.client.Snippet;
import cloud.codestore.client.ui.FxController;
import cloud.codestore.client.ui.selection.list.CreateSnippetEvent;
import cloud.codestore.client.ui.selection.list.SnippetSelectedEvent;
import cloud.codestore.client.ui.snippet.code.SnippetCode;
import cloud.codestore.client.ui.snippet.description.SnippetDescription;
import cloud.codestore.client.ui.snippet.details.SnippetDetails;
import cloud.codestore.client.ui.snippet.footer.Footer;
import cloud.codestore.client.ui.snippet.title.SnippetTitle;
import cloud.codestore.client.usecases.deletesnippet.DeleteSnippetUseCase;
import cloud.codestore.client.usecases.readsnippet.ReadSnippetUseCase;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;

import javax.annotation.Nonnull;
import java.util.Collections;

@FxController
public class SnippetController {
    private ReadSnippetUseCase readSnippetUseCase;
    private DeleteSnippetUseCase deleteSnippetUseCase;
    private String currentSnippet;

    private BooleanProperty editingProperty = new SimpleBooleanProperty(false);

    @FXML
    private SnippetTitle snippetTitleController;
    @FXML
    private SnippetDescription snippetDescriptionController;
    @FXML
    private SnippetCode snippetCodeController;
    @FXML
    private SnippetDetails snippetDetailsController;
    @FXML
    private Footer snippetFooterController;

    SnippetController(
            @Nonnull ReadSnippetUseCase readSnippetUseCase,
            @Nonnull DeleteSnippetUseCase deleteSnippetUseCase,
            @Nonnull EventBus eventBus
    ) {
        this.readSnippetUseCase = readSnippetUseCase;
        this.deleteSnippetUseCase = deleteSnippetUseCase;
        eventBus.register(this);
    }

    @FXML
    private void initialize() {
        snippetTitleController.bindEditing(editingProperty);
        snippetDescriptionController.bindEditing(editingProperty);
        snippetCodeController.bindEditing(editingProperty);
        snippetDetailsController.bindEditing(editingProperty);
        snippetFooterController.bindEditing(editingProperty);
        snippetFooterController.onDelete(() -> deleteSnippetUseCase.deleteSnippet(currentSnippet));
    }

    @Subscribe
    private void snippetSelected(@Nonnull SnippetSelectedEvent event) {
        currentSnippet = event.snippetUri();
        editingProperty.set(false);
        Snippet snippet = readSnippetUseCase.readSnippet(currentSnippet);
        snippetTitleController.setText(snippet.getTitle());
        snippetDescriptionController.setText(snippet.getDescription());
        snippetCodeController.setText(snippet.getCode());
        snippetDetailsController.setTags(snippet.getTags());
        snippetFooterController.setPermissions(snippet.getPermissions());
    }

    @Subscribe
    private void createSnippet(@Nonnull CreateSnippetEvent event) {
        snippetTitleController.setText("");
        snippetDescriptionController.setText("");
        snippetCodeController.setText("");
        snippetDetailsController.setTags(Collections.emptyList());
        snippetFooterController.setPermissions(Collections.emptySet());
        editingProperty.set(true);
    }
}
