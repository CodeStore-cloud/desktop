package cloud.codestore.client.ui.snippet;

import cloud.codestore.client.Language;
import cloud.codestore.client.Permission;
import cloud.codestore.client.Snippet;
import cloud.codestore.client.SnippetBuilder;
import cloud.codestore.client.ui.selection.history.History;
import cloud.codestore.client.ui.selection.list.CreateSnippetEvent;
import cloud.codestore.client.ui.selection.list.RequestSnippetSelectionEvent;
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
import cloud.codestore.client.usecases.updatesnippet.UpdateSnippetUseCase;
import cloud.codestore.client.usecases.updatesnippet.UpdatedSnippetDto;
import com.google.common.eventbus.EventBus;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.testfx.framework.junit5.ApplicationTest;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("The snippet controller")
class SnippetControllerTest extends ApplicationTest {
    private static final String SNIPPET_ID = "1";
    private static final String SNIPPET_URI = "snippets/1";
    private static final Snippet EMPTY_SNIPPET = Snippet.builder().build();

    private ReadSnippetUseCase readSnippetUseCase = mock(ReadSnippetUseCase.class);
    private DeleteSnippetUseCase deleteSnippetUseCase = mock(DeleteSnippetUseCase.class);
    private CreateSnippetUseCase createSnippetUseCase = mock(CreateSnippetUseCase.class);
    private UpdateSnippetUseCase updateSnippetUseCase = mock(UpdateSnippetUseCase.class);
    private EventBus eventBus = spy(new EventBus());

    @Mock
    private SnippetTitle snippetTitleController;
    @Mock
    private SnippetDescription snippetDescriptionController;
    @Mock
    private SnippetCode snippetCodeController;
    @Mock
    private SnippetDetails snippetDetailsController;
    @Spy
    private DummyFooter snippetFooterController = new DummyFooter();

    @Mock
    private Pane snippetPane;
    @Mock
    private Pane noSnippetLabel;
    @Mock
    private History history;

    @InjectMocks
    private SnippetController snippetController = new SnippetController(
            readSnippetUseCase, createSnippetUseCase, updateSnippetUseCase, deleteSnippetUseCase, eventBus
    );

    private Snippet testSnippet;

    @BeforeEach
    void setUp() throws Exception {
        testSnippet = Snippet.builder()
                             .id(SNIPPET_ID)
                             .uri(SNIPPET_URI)
                             .title("A random title")
                             .description("With a short description")
                             .code("System.out.println(\"Hello, World!\");")
                             .language(new Language("Java", "10"))
                             .tags(List.of("hello", "world"))
                             .permissions(Set.of(Permission.DELETE))
                             .build();

        lenient().when(readSnippetUseCase.readSnippet(SNIPPET_URI)).thenReturn(testSnippet);
        callInitialize();
    }

    @Nested
    @DisplayName("when not editing")
    class NotEditingState {
        @Test
        @DisplayName("shows the selected code snippet")
        void loadSnippet() {
            clearInvocations();

            requestSnippetSelection();

            verifyShowSnippetPane();
            verifyEditable(false);
            verifyVisit(testSnippet);
        }

        @Test
        @DisplayName("clears all values when a CreateSnippetEvent is triggered")
        void newSnippet() {
            clearInvocations();

            eventBus.post(new CreateSnippetEvent());

            verify(readSnippetUseCase, never()).readSnippet(anyString());
            verifyShowSnippetPane();
            verifyVisit(EMPTY_SNIPPET);
            verifyEditable(true);
        }

        @Test
        @DisplayName("deletes the current snippet after confirmation")
        void deleteSnippet() {
            requestSnippetSelection();
            clearInvocations();

            interact(snippetFooterController::clickDeleteButton);
            clickOn("#yes");

            verify(deleteSnippetUseCase).deleteSnippet(SNIPPET_URI);
            verifyVisit(EMPTY_SNIPPET);
            verifyEditable(false);
            verify(eventBus).post(new SnippetDeletedEvent(SNIPPET_URI));
        }
    }

    @Nested
    @DisplayName("when creating a new snippet")
    class NewSnippetState {
        @BeforeEach
        void setUp() {
            eventBus.post(new CreateSnippetEvent());
            verifyEditable(true);
            clearInvocations();
        }

        @Test
        @DisplayName("creates a new snippet based on the input")
        void createSnippet() {
            var title = "A new snippet";
            var description = "A description";
            var code = "print(\"Hello, World!\");";
            var language = new Language("Python", "1");
            var tags = List.of("python", "test");

            Snippet createdSnippet = prepareDataCollection(title, description, language, code, tags);
            var dtoArgument = ArgumentCaptor.forClass(NewSnippetDto.class);
            when(createSnippetUseCase.create(any(NewSnippetDto.class))).thenReturn(createdSnippet);

            snippetFooterController.clickSaveButton();

            verify(createSnippetUseCase).create(dtoArgument.capture());
            NewSnippetDto expectedDto = new NewSnippetDto(title, description, language, code, tags);
            assertThat(dtoArgument.getValue()).isEqualTo(expectedDto);

            verifyEditable(false);
            verifyVisit(createdSnippet);
            verify(eventBus).post(new SnippetCreatedEvent(SNIPPET_URI));
        }

        @Test
        @DisplayName("clears the input when editing is canceled")
        void clearInput() {
            snippetFooterController.clickCancelButton();

            verifyVisit(EMPTY_SNIPPET);
            verifyEditable(false);
        }

        @Nested
        @DisplayName("when another snippet is selected")
        class RequestSnippetSelectionTest {
            private static final String CREATED_SNIPPET_URI = SNIPPET_URI;
            private static final String SELECTED_SNIPPET_URI = "snippets/2";

            @BeforeEach
            void setUp() {
                Snippet selectedSnippet = Snippet.builder().uri(SELECTED_SNIPPET_URI).build();
                when(readSnippetUseCase.readSnippet(SELECTED_SNIPPET_URI)).thenReturn(selectedSnippet);

                interact(() -> requestSnippetSelection(SELECTED_SNIPPET_URI));
            }

            @Test
            @DisplayName("saves the current snippet and selects the new one when the user confirms saving")
            void confirmSaving() {
                Snippet createdSnippet = Snippet.builder().uri(CREATED_SNIPPET_URI).build();
                when(createSnippetUseCase.create(any())).thenReturn(createdSnippet);

                clickOn("#yes");

                verify(eventBus).post(new SnippetCreatedEvent(CREATED_SNIPPET_URI));
                verify(eventBus).post(new SnippetSelectedEvent(SELECTED_SNIPPET_URI));
            }

            @Test
            @DisplayName("does not save the current snippet and selects the new one when the user rejected saving")
            void rejectSaving() {
                clickOn("#no");
                verify(eventBus, never()).post(new SnippetCreatedEvent(CREATED_SNIPPET_URI));
                verify(eventBus).post(new SnippetSelectedEvent(SELECTED_SNIPPET_URI));
            }

            @Test
            @DisplayName("does nothing when the user cancels saving")
            void cancelSaving() {
                clickOn("#cancel");
                verify(eventBus, never()).post(new SnippetCreatedEvent(CREATED_SNIPPET_URI));
                verify(eventBus, never()).post(new SnippetSelectedEvent(SELECTED_SNIPPET_URI));
            }
        }
    }

    @Nested
    @DisplayName("when editing a snippet")
    class EditSnippetState {
        @BeforeEach
        void setUp() {
            requestSnippetSelection();
            snippetFooterController.clickEditButton();
            verifyEditable(true);
            clearInvocations();
        }

        @Test
        @DisplayName("updates a snippet based on the input")
        void updateSnippet() {
            var title = "An updated snippet";
            var description = "With a new description";
            var code = "System.out.println(\"Hello, World!\");";
            var language = new Language("Java", "2");
            var tags = List.of("java", "hello", "world");

            Snippet updatedSnippet = prepareDataCollection(title, description, language, code, tags);

            var dtoArgument = ArgumentCaptor.forClass(UpdatedSnippetDto.class);
            when(updateSnippetUseCase.update(any(UpdatedSnippetDto.class))).thenReturn(updatedSnippet);

            snippetFooterController.clickSaveButton();

            verify(updateSnippetUseCase).update(dtoArgument.capture());
            UpdatedSnippetDto expectedDto = new UpdatedSnippetDto(SNIPPET_ID, SNIPPET_URI, title, description, language, code, tags);
            assertThat(dtoArgument.getValue()).isEqualTo(expectedDto);

            verifyEditable(false);
            verifyVisit(updatedSnippet);
            verify(eventBus).post(new SnippetUpdatedEvent(SNIPPET_URI));
        }

        @Test
        @DisplayName("resets the input when editing is canceled")
        void clearInput() {
            snippetFooterController.clickCancelButton();

            verifyVisit(testSnippet);
            verifyEditable(false);
        }

        @Nested
        @DisplayName("when another snippet is selected")
        class RequestSnippetSelectionTest {
            private static final String CURRENT_SNIPPET_URI = SNIPPET_URI;
            private static final String SELECTED_SNIPPET_URI = "snippets/2";

            @BeforeEach
            void setUp() {
                Snippet selectedSnippet = Snippet.builder().uri(SELECTED_SNIPPET_URI).build();
                when(readSnippetUseCase.readSnippet(SELECTED_SNIPPET_URI)).thenReturn(selectedSnippet);

                interact(() -> requestSnippetSelection(SELECTED_SNIPPET_URI));
                clearInvocations();
            }

            @Test
            @DisplayName("saves the current snippet and selects the new one when the user confirms saving")
            void confirmSaving() {
                Snippet updatedSnippet = Snippet.builder().uri(CURRENT_SNIPPET_URI).build();
                when(updateSnippetUseCase.update(any())).thenReturn(updatedSnippet);

                clickOn("#yes");

                verify(eventBus).post(new SnippetUpdatedEvent(CURRENT_SNIPPET_URI));
                verify(eventBus).post(new SnippetSelectedEvent(SELECTED_SNIPPET_URI));
            }

            @Test
            @DisplayName("does not save the current snippet and selects the new one when the user rejected saving")
            void rejectSaving() {
                clickOn("#no");

                verify(eventBus, never()).post(new SnippetUpdatedEvent(CURRENT_SNIPPET_URI));
                verify(eventBus).post(new SnippetSelectedEvent(SELECTED_SNIPPET_URI));
            }

            @Test
            @DisplayName("does nothing when the user cancels saving")
            void cancelSaving() {
                clickOn("#cancel");
                verify(eventBus, never()).post(new SnippetUpdatedEvent(SNIPPET_URI));
                verify(eventBus, never()).post(new SnippetSelectedEvent(SNIPPET_URI));
            }
        }
    }

    private void verifyEditable(boolean editable) {
        verify(snippetTitleController).setEditing(editable);
        verify(snippetDescriptionController).setEditing(editable);
        verify(snippetCodeController).setEditing(editable);
        verify(snippetDetailsController).setEditing(editable);
        verify(snippetFooterController).setEditing(editable);
        verify(history).setEditing(editable);
    }

    private void verifyShowSnippetPane() {
        verify(noSnippetLabel).setVisible(false);
        verify(snippetPane).setVisible(true);
    }

    private void verifyVisit(Snippet snippet) {
        verify(snippetTitleController).visit(
                argThat((Snippet arg) -> Objects.equals(arg.getTitle(), snippet.getTitle()))
        );
        verify(snippetDescriptionController).visit(
                argThat((Snippet arg) -> Objects.equals(arg.getDescription(), snippet.getDescription()))
        );
        verify(snippetCodeController).visit(
                argThat((Snippet arg) -> Objects.equals(arg.getCode(), snippet.getCode()) &&
                                         Objects.equals(arg.getLanguage(), snippet.getLanguage()))
        );
        verify(snippetDetailsController).visit(
                argThat((Snippet arg) -> Objects.equals(arg.getTags(), snippet.getTags()))
        );
        verify(snippetFooterController).visit(
                argThat((Snippet arg) -> Objects.equals(arg.getPermissions(), snippet.getPermissions()))
        );
    }

    private Answer<Void> answer(Consumer<SnippetBuilder> consumer) {
        return invocation -> {
            SnippetBuilder builder = invocation.getArgument(0, SnippetBuilder.class);
            consumer.accept(builder);
            return null;
        };
    }

    private void clearInvocations() {
        Mockito.clearInvocations(
                snippetTitleController,
                snippetDescriptionController,
                snippetCodeController,
                snippetDetailsController,
                snippetFooterController,
                history,
                eventBus
        );
    }

    private void callInitialize() throws Exception {
        Method method = SnippetController.class.getDeclaredMethod("initialize");
        method.setAccessible(true);
        method.invoke(snippetController);
    }

    private Snippet prepareDataCollection(
            String title,
            String description,
            Language language,
            String code,
            List<String> tags
    ) {
        lenient().doAnswer(answer(builder -> builder.title(title)))
                 .when(snippetTitleController).visit(any(SnippetBuilder.class));

        lenient().doAnswer(answer(builder -> builder.description(description)))
                 .when(snippetDescriptionController).visit(any(SnippetBuilder.class));

        lenient().doAnswer(answer(builder -> builder.code(code).language(language)))
                 .when(snippetCodeController).visit(any(SnippetBuilder.class));

        lenient().doAnswer(answer(builder -> builder.tags(tags)))
                 .when(snippetDetailsController).visit(any(SnippetBuilder.class));

        return Snippet.builder()
                      .id(SNIPPET_ID)
                      .uri(SNIPPET_URI)
                      .title(title)
                      .description(description)
                      .code(code)
                      .language(language)
                      .tags(tags)
                      .build();
    }

    private void requestSnippetSelection() {
        requestSnippetSelection(SNIPPET_URI);
    }

    private void requestSnippetSelection(String uri) {
        eventBus.post(new RequestSnippetSelectionEvent(uri));
    }

    private static class DummyFooter extends SnippetFooter {
        private EventHandler<ActionEvent> onSave;
        private EventHandler<ActionEvent> onCancel;
        private EventHandler<ActionEvent> onEdit;
        private EventHandler<ActionEvent> onDelete;

        @Override
        public void onSave(EventHandler<ActionEvent> callback) {
            onSave = callback;
        }

        @Override
        public void onCancel(EventHandler<ActionEvent> callback) {
            onCancel = callback;
        }

        @Override
        public void onEdit(EventHandler<ActionEvent> callback) {
            onEdit = callback;
        }

        @Override
        public void onDelete(EventHandler<ActionEvent> callback) {
            onDelete = callback;
        }

        void clickSaveButton() {
            onSave.handle(null);
        }

        void clickCancelButton() {
            onCancel.handle(null);
        }

        void clickEditButton() {
            onEdit.handle(null);
        }

        void clickDeleteButton() {
            onDelete.handle(null);
        }

        @Override
        public void setEditing(boolean editing) {}

        @Override
        public void visit(@Nonnull Snippet snippet) {}
    }
}