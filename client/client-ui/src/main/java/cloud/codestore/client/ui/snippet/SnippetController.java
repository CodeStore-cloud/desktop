package cloud.codestore.client.ui.snippet;

import cloud.codestore.client.Snippet;
import cloud.codestore.client.SnippetBuilder;
import cloud.codestore.client.ui.FxController;
import cloud.codestore.client.ui.selection.history.History;
import cloud.codestore.client.ui.selection.list.CreateSnippetEvent;
import cloud.codestore.client.ui.snippet.code.SnippetCode;
import cloud.codestore.client.ui.snippet.description.SnippetDescription;
import cloud.codestore.client.ui.snippet.details.SnippetDetails;
import cloud.codestore.client.ui.snippet.footer.SnippetFooter;
import cloud.codestore.client.ui.snippet.title.SnippetTitle;
import cloud.codestore.client.usecases.createsnippet.CreateSnippetUseCase;
import cloud.codestore.client.usecases.createsnippet.NewSnippetDto;
import cloud.codestore.client.usecases.deletesnippet.DeleteSnippetUseCase;
import cloud.codestore.client.usecases.readsnippet.ReadSnippetUseCase;
import cloud.codestore.client.usecases.updatesnippet.UpdateSnippetUseCase;
import cloud.codestore.client.usecases.updatesnippet.UpdatedSnippetDto;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

import javax.annotation.Nonnull;

@FxController
public class SnippetController {
    private static final Snippet EMPTY_SNIPPET = Snippet.builder().build();

    private final ReadSnippetUseCase readSnippetUseCase;
    private final CreateSnippetUseCase createSnippetUseCase;
    private final UpdateSnippetUseCase updateSnippetUseCase;
    private final DeleteSnippetUseCase deleteSnippetUseCase;
    private final EventBus eventBus;
    private final AsyncStringProperty selectedSnippetProperty = new AsyncStringProperty();

    private Snippet currentSnippet;
    private ControllerState state;
    private SnippetForm[] forms;

    @FXML
    private Pane snippetPane;
    @FXML
    private Pane noSnippetLabel;

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
    @FXML
    private History historyController;

    SnippetController(
            @Nonnull ReadSnippetUseCase readSnippetUseCase,
            @Nonnull CreateSnippetUseCase createSnippetUseCase,
            @Nonnull UpdateSnippetUseCase updateSnippetUseCase,
            @Nonnull DeleteSnippetUseCase deleteSnippetUseCase,
            @Nonnull EventBus eventBus
    ) {
        this.readSnippetUseCase = readSnippetUseCase;
        this.createSnippetUseCase = createSnippetUseCase;
        this.updateSnippetUseCase = updateSnippetUseCase;
        this.deleteSnippetUseCase = deleteSnippetUseCase;
        this.eventBus = eventBus;
        eventBus.register(this);
    }

    @FXML
    private void initialize() {
        forms = new SnippetForm[]{
                historyController,
                snippetTitleController,
                snippetDescriptionController,
                snippetCodeController,
                snippetDetailsController,
                snippetFooterController
        };

        snippetFooterController.onSave(event -> state.save());
        snippetFooterController.onCancel(event -> state.cancel());
        snippetFooterController.onEdit(event -> state.edit());
        snippetFooterController.onDelete(event -> state.delete());

        historyController.setSelectedSnippetProperty(selectedSnippetProperty);
        state = new DefaultState();
        registerSelectionHandler();
    }

    /**
     * @return a Property-Object that contains the URI of the currently selected code snippet.
     */
    @Nonnull
    public StringProperty selectedSnippetProperty() {
        return selectedSnippetProperty;
    }

    private void registerSelectionHandler() {
        selectedSnippetProperty.onChangeRequested(snippetUri -> {
            Runnable selectSnippet = () -> {
                Snippet snippet = readSnippetUseCase.readSnippet(snippetUri);
                state = new ShowSnippetState(snippet);
            };

            if (state.isEditing()) {
                requestSaving(selectSnippet);
            } else {
                selectSnippet.run();
            }
        });
    }

    @Subscribe
    private void createSnippet(@Nonnull CreateSnippetEvent event) {
        if (state.isEditing()) {
            requestSaving(() -> state = new NewSnippetState());
        } else {
            state = new NewSnippetState();
        }
    }

    private void requestSaving(Runnable action) {
        new ConfirmationDialog("dialog.confirm.saveSnippet.title", "dialog.confirm.saveSnippet.message")
                .onYes(() -> {
                    state.save();
                    action.run();
                })
                .onNo(action)
                .show();
    }

    private void setEditing(boolean editing) {
        for (SnippetForm form : forms) {
            form.setEditing(editing);
        }
    }

    private void accept(Snippet snippet) {
        currentSnippet = snippet;
        selectedSnippetProperty.setFinally(snippet.getUri());
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

        /**
         * Called whenever the user wants to edit the current snippet.
         */
        default void edit() {}

        /**
         * @return whether this state is a state in which the user is editing a code snippet.
         */
        default boolean isEditing() {
            return false;
        }
    }

    /**
     * The default state showing an empty snippet.
     */
    private class DefaultState implements ControllerState {
        DefaultState() {
            accept(EMPTY_SNIPPET);
            setEditing(false);

            noSnippetLabel.setVisible(true);
            snippetPane.setVisible(false);
        }
    }

    /**
     * The state for showing a code snippet.
     */
    private class ShowSnippetState implements ControllerState {
        ShowSnippetState(@Nonnull Snippet snippet) {
            noSnippetLabel.setVisible(false);
            snippetPane.setVisible(true);

            accept(snippet);
            setEditing(false);
        }

        @Override
        public void edit() {
            state = new EditSnippetState();
        }

        @Override
        public void delete() {
            new ConfirmationDialog("dialog.confirm.title", "dialog.confirm.deleteSnippet.message")
                    .setCancellable(false)
                    .onYes(() -> {
                        String snippetUri = currentSnippet.getUri();
                        deleteSnippetUseCase.deleteSnippet(snippetUri);
                        state = new DefaultState();
                        eventBus.post(new SnippetDeletedEvent(snippetUri));
                    })
                    .show();
        }
    }

    /**
     * The state for creating a new code snippet.
     */
    private class NewSnippetState implements ControllerState {
        NewSnippetState() {
            noSnippetLabel.setVisible(false);
            snippetPane.setVisible(true);

            accept(EMPTY_SNIPPET);
            setEditing(true);
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
            SnippetBuilder builder = Snippet.builder();
            accept(builder);
            return builder.build();
        }

        @Override
        public void cancel() {
            state = new DefaultState();
        }

        @Override
        public boolean isEditing() {
            return true;
        }
    }

    /**
     * The state for editing a code snippet.
     */
    private class EditSnippetState implements ControllerState {
        EditSnippetState() {
            setEditing(true);
        }

        @Override
        public void save() {
            Snippet snippet = collectSnippetData();
            UpdatedSnippetDto dto = new UpdatedSnippetDto(
                    currentSnippet.getId(),
                    currentSnippet.getUri(),
                    snippet.getTitle(),
                    snippet.getDescription(),
                    snippet.getLanguage(),
                    snippet.getCode(),
                    snippet.getTags()
            );

            Snippet updatedSnippet = updateSnippetUseCase.update(dto);
            state = new ShowSnippetState(updatedSnippet);
            eventBus.post(new SnippetUpdatedEvent(updatedSnippet.getUri()));
        }

        private Snippet collectSnippetData() {
            SnippetBuilder builder = Snippet.builder().id(currentSnippet.getId()).uri(currentSnippet.getUri());
            accept(builder);
            return builder.build();
        }

        @Override
        public void cancel() {
            state = new ShowSnippetState(currentSnippet);
        }

        @Override
        public boolean isEditing() {
            return true;
        }
    }
}
