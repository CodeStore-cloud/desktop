package cloud.codestore.client.ui.snippet;

import cloud.codestore.client.Language;
import cloud.codestore.client.Permission;
import cloud.codestore.client.Snippet;
import cloud.codestore.client.SnippetBuilder;
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
import cloud.codestore.client.usecases.updatesnippet.UpdateSnippetUseCase;
import cloud.codestore.client.usecases.updatesnippet.UpdatedSnippetDto;
import com.google.common.eventbus.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

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
class SnippetControllerTest {
    private static final String SNIPPET_ID = "1";
    private static final String SNIPPET_URI = "http://localhost:8080/snippets/1";
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
    private TestFooter snippetFooterController = new TestFooter();

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

        lenient().when(readSnippetUseCase.readSnippet(anyString())).thenReturn(testSnippet);
        callInitialize();
    }

    @Nested
    @DisplayName("when not editing")
    class NotEditingState {
        @Test
        @DisplayName("shows the selected code snippet")
        void loadSnippet() {
            clearInvocations();

            selectSnippet();

            verifyEditable(false);
            verifyVisit(testSnippet);
        }

        @Test
        @DisplayName("clears all values when a CreateSnippetEvent is triggered")
        void newSnippet() {
            clearInvocations();

            eventBus.post(new CreateSnippetEvent());

            verify(readSnippetUseCase, never()).readSnippet(anyString());
            verifyVisit(EMPTY_SNIPPET);
            verifyEditable(true);
        }

        @Test
        @DisplayName("deletes the current snippet")
        void deleteSnippet() {
            selectSnippet();
            clearInvocations();

            snippetFooterController.clickDeleteButton();

            verify(deleteSnippetUseCase).deleteSnippet(SNIPPET_URI);
            verifyVisit(EMPTY_SNIPPET);
            verifyEditable(false);
            verify(eventBus).post(new SnippetDeletedEvent(SNIPPET_URI));
        }
    }

    private void selectSnippet() {
        eventBus.post(new SnippetSelectedEvent(SNIPPET_URI));
    }

    @Nested
    @DisplayName("when creating a new snippet")
    class NewSnippetState {
        @Test
        @DisplayName("clears the input when the edit is canceled")
        void clearInput() {
            eventBus.post(new CreateSnippetEvent());
            verifyEditable(true);
            clearInvocations();

            snippetFooterController.clickCancelButton();

            verifyVisit(EMPTY_SNIPPET);
            verifyEditable(false);
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

            eventBus.post(new CreateSnippetEvent());
            clearInvocations();

            snippetFooterController.clickSaveButton();

            verify(createSnippetUseCase).create(dtoArgument.capture());
            NewSnippetDto expectedDto = new NewSnippetDto(title, description, language, code, tags);
            assertThat(dtoArgument.getValue()).isEqualTo(expectedDto);

            verifyEditable(false);
            verifyVisit(createdSnippet);
            verify(eventBus).post(new SnippetCreatedEvent(SNIPPET_URI));
        }
    }

    @Nested
    @DisplayName("when editing a snippet")
    class EditSnippetState {
        @Test
        @DisplayName("resets the input when the edit is canceled")
        void clearInput() {
            selectSnippet();
            snippetFooterController.clickEditButton();
            verifyEditable(true);
            clearInvocations();

            snippetFooterController.clickCancelButton();

            verifyVisit(testSnippet);
            verifyEditable(false);
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

            selectSnippet();
            snippetFooterController.clickEditButton();
            clearInvocations();

            snippetFooterController.clickSaveButton();

            verify(updateSnippetUseCase).update(dtoArgument.capture());
            UpdatedSnippetDto expectedDto = new UpdatedSnippetDto(SNIPPET_ID, SNIPPET_URI, title, description, language, code, tags);
            assertThat(dtoArgument.getValue()).isEqualTo(expectedDto);

            verifyEditable(false);
            verifyVisit(updatedSnippet);
            verify(eventBus).post(new SnippetUpdatedEvent(SNIPPET_URI));
        }
    }

    private void verifyEditable(boolean editable) {
        verify(snippetTitleController).setEditing(editable);
        verify(snippetDescriptionController).setEditing(editable);
        verify(snippetCodeController).setEditing(editable);
        verify(snippetDetailsController).setEditing(editable);
        verify(snippetFooterController).setEditing(editable);
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
                snippetFooterController
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

    private static class TestFooter extends SnippetFooter {
        private Runnable onSave;
        private Runnable onCancel;
        private Runnable onEdit;
        private Runnable onDelete;

        @Override
        public void onSave(Runnable callback) {
            onSave = callback;
        }

        @Override
        public void onCancel(Runnable callback) {
            onCancel = callback;
        }

        @Override
        public void onEdit(Runnable callback) {
            onEdit = callback;
        }

        @Override
        public void onDelete(Runnable callback) {
            onDelete = callback;
        }

        void clickSaveButton() {
            onSave.run();
        }

        void clickCancelButton() {
            onCancel.run();
        }

        void clickEditButton() {
            onEdit.run();
        }

        void clickDeleteButton() {
            onDelete.run();
        }

        @Override
        public void setEditing(boolean editing) {}

        @Override
        public void visit(@Nonnull Snippet snippet) {}
    }
}