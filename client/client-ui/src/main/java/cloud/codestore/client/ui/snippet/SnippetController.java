package cloud.codestore.client.ui.snippet;

import cloud.codestore.client.Snippet;
import cloud.codestore.client.ui.FxController;
import cloud.codestore.client.ui.selection.list.SnippetSelectedEvent;
import cloud.codestore.client.ui.snippet.code.SnippetCode;
import cloud.codestore.client.ui.snippet.description.SnippetDescription;
import cloud.codestore.client.ui.snippet.title.SnippetTitle;
import cloud.codestore.client.usecases.readsnippet.ReadSnippet;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.fxml.FXML;

import javax.annotation.Nonnull;

@FxController
public class SnippetController {
    private ReadSnippet readSnippetUseCase;

    @FXML
    private SnippetTitle snippetTitleController;
    @FXML
    private SnippetDescription snippetDescriptionController;
    @FXML
    private SnippetCode snippetCodeController;

    SnippetController(@Nonnull ReadSnippet readSnippetUseCase, @Nonnull EventBus eventBus) {
        eventBus.register(this);
        this.readSnippetUseCase = readSnippetUseCase;
    }

    @Subscribe
    private void snippetSelected(@Nonnull SnippetSelectedEvent event) {
        Snippet snippet = readSnippetUseCase.readSnippet(event.snippetUri());
        snippetTitleController.setText(snippet.getTitle());
        snippetDescriptionController.setText(snippet.getDescription());
        snippetCodeController.setText(snippet.getCode());
    }
}
