package cloud.codestore.client.ui.snippet;

import cloud.codestore.client.Snippet;
import cloud.codestore.client.ui.FxController;
import cloud.codestore.client.ui.selection.list.CreateSnippetEvent;
import cloud.codestore.client.ui.selection.list.SnippetSelectedEvent;
import cloud.codestore.client.ui.snippet.code.SnippetCode;
import cloud.codestore.client.ui.snippet.description.SnippetDescription;
import cloud.codestore.client.ui.snippet.details.SnippetDetails;
import cloud.codestore.client.ui.snippet.footer.SnippetFooter;
import cloud.codestore.client.ui.snippet.title.SnippetTitle;
import cloud.codestore.client.usecases.createsnippet.CreateSnippetUseCase;
import cloud.codestore.client.usecases.createsnippet.NewSnippetDto;
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
    private CreateSnippetUseCase createSnippetUseCase;
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
    private SnippetFooter snippetFooterController;

    SnippetController(
            @Nonnull ReadSnippetUseCase readSnippetUseCase,
            @Nonnull CreateSnippetUseCase createSnippetUseCase,
            @Nonnull DeleteSnippetUseCase deleteSnippetUseCase,
            @Nonnull EventBus eventBus
    ) {
        this.readSnippetUseCase = readSnippetUseCase;
        this.createSnippetUseCase = createSnippetUseCase;
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
        show(readSnippetUseCase.readSnippet(event.snippetUri()));
    }

    @Subscribe
    private void createSnippet(@Nonnull CreateSnippetEvent event) {
        clear();
        snippetFooterController.onSave(this::saveNewSnippet);
        snippetFooterController.onCancel(this::clear);
        editingProperty.set(true);
    }

    private void show(@Nonnull Snippet snippet) {
        editingProperty.set(false);
        currentSnippet = snippet.getUri();

        snippetTitleController.setText(snippet.getTitle());
        snippetDescriptionController.setText(snippet.getDescription());
        snippetCodeController.setText(snippet.getCode());
        snippetCodeController.setLanguage(snippet.getLanguage());
        snippetDetailsController.setTags(snippet.getTags());
        snippetFooterController.setPermissions(snippet.getPermissions());
    }

    private void saveNewSnippet() {
        var dto = new NewSnippetDto(
                snippetTitleController.getText(),
                snippetDescriptionController.getText(),
                snippetCodeController.getLanguage(),
                snippetCodeController.getText(),
                snippetDetailsController.getTags()
        );

        Snippet createdSnippet = createSnippetUseCase.create(dto);
        show(createdSnippet);
    }

    private void clear() {
        editingProperty.set(false);
        currentSnippet = "";

        snippetTitleController.setText("");
        snippetDescriptionController.setText("");
        snippetCodeController.setText("");
        snippetCodeController.setLanguage(null);
        snippetDetailsController.setTags(Collections.emptyList());
        snippetFooterController.setPermissions(Collections.emptySet());
    }
}
