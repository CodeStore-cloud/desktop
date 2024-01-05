package cloud.codestore.client.ui.snippet;

import cloud.codestore.client.Snippet;
import cloud.codestore.client.SnippetBuilder;
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
import javafx.fxml.FXML;

import javax.annotation.Nonnull;

@FxController
public class SnippetController {
    private static final Snippet EMPTY_SNIPPET = new SnippetBuilder().uri("").build();

    private final ReadSnippetUseCase readSnippetUseCase;
    private final CreateSnippetUseCase createSnippetUseCase;
    private final DeleteSnippetUseCase deleteSnippetUseCase;
    private final EventBus eventBus;

    private String currentSnippetUri;
    private ControllerState state;
    private SnippetForm[] forms;

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
        this.eventBus = eventBus;
        eventBus.register(this);
    }

    @FXML
    private void initialize() {
        forms = new SnippetForm[]{
                snippetTitleController,
                snippetDescriptionController,
                snippetCodeController,
                snippetDetailsController,
                snippetFooterController
        };

        snippetFooterController.onSave(() -> state.save());
        snippetFooterController.onCancel(() -> state.cancel());
        snippetFooterController.onDelete(() -> state.delete());

        state = new DefaultState();
    }

    @Subscribe
    private void snippetSelected(@Nonnull SnippetSelectedEvent event) {
        Snippet snippet = readSnippetUseCase.readSnippet(event.snippetUri());
        state = new ShowSnippetState(snippet);
    }

    @Subscribe
    private void createSnippet(@Nonnull CreateSnippetEvent event) {
        state = new NewSnippetState();
    }

    private void setEditable(boolean editable) {
        for (SnippetForm form : forms) {
            form.setEditable(editable);
        }
    }

    private void accept(Snippet snippet) {
        currentSnippetUri = snippet.getUri();
        for (SnippetForm form : forms) {
            form.visit(snippet);
        }
    }

    private void accept(SnippetBuilder builder) {
        for (SnippetForm form : forms) {
            form.visit(builder);
        }
    }

    /**
     * Represents a state of this controller.
     */
    private interface ControllerState {
        /**
         * Called whenever the user saves the edit.
         */
        default void save() {}

        /**
         * Called whenever the user cancels the edit.
         */
        default void cancel() {}

        /**
         * Called whenever the user wants to delete the current snippet.
         */
        default void delete() {}
    }

    /**
     * The default state showing an empty snippet.
     */
    private class DefaultState implements ControllerState {
        DefaultState() {
            setEditable(false);
            accept(EMPTY_SNIPPET);
        }
    }

    /**
     * The state for showing a code snippet.
     */
    private class ShowSnippetState implements ControllerState {
        ShowSnippetState(@Nonnull Snippet snippet) {
            setEditable(false);
            accept(snippet);
        }

        @Override
        public void delete() {
            String snippetUri = currentSnippetUri;
            deleteSnippetUseCase.deleteSnippet(snippetUri);
            state = new DefaultState();
            eventBus.post(new SnippetDeletedEvent(snippetUri));
        }
    }

    /**
     * The state for creating a new code snippet.
     */
    private class NewSnippetState implements ControllerState {
        NewSnippetState() {
            accept(EMPTY_SNIPPET);
            setEditable(true);
        }

        @Override
        public void save() {
            Snippet snippet = collectSnippetData();
            NewSnippetDto dto = new NewSnippetDto(
                    snippet.getTitle(),
                    snippet.getDescription(),
                    snippet.getLanguage(),
                    snippet.getCode(),
                    snippet.getTags()
            );

            Snippet createdSnippet = createSnippetUseCase.create(dto);
            state = new ShowSnippetState(createdSnippet);
            eventBus.post(new SnippetCreatedEvent(createdSnippet.getUri()));
        }

        private Snippet collectSnippetData() {
            SnippetBuilder builder = new SnippetBuilder().uri("");
            accept(builder);
            return builder.build();
        }

        @Override
        public void cancel() {
            state = new DefaultState();
        }
    }
}
