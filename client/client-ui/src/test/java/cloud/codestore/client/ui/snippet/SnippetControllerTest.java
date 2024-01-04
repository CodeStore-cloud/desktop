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
import com.google.common.eventbus.EventBus;
import javafx.beans.property.BooleanProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("The snippet controller")
class SnippetControllerTest {
    private static final String SNIPPET_URI = "http://localhost:8080/snippets/1";

    @Mock
    private ReadSnippetUseCase readSnippetUseCase;
    @Mock
    private DeleteSnippetUseCase deleteSnippetUseCase;
    @Mock
    private CreateSnippetUseCase createSnippetUseCase;
    private EventBus eventBus = new EventBus();

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
            readSnippetUseCase, createSnippetUseCase, deleteSnippetUseCase, eventBus
    );

    private Snippet testSnippet;

    @BeforeEach
    void setUp() {
        testSnippet = Snippet.builder()
                             .uri(SNIPPET_URI)
                             .title("A random title")
                             .description("With a short description")
                             .code("System.out.println(\"Hello, World!\");")
                             .language(new Language("Java", "10"))
                             .tags(List.of("hello", "world"))
                             .permissions(Set.of(Permission.DELETE))
                             .build();

        lenient().when(readSnippetUseCase.readSnippet(anyString())).thenReturn(testSnippet);
    }

    @Test
    @DisplayName("loads a code snippet when selected")
    void loadSnippet() {
        eventBus.post(new SnippetSelectedEvent(SNIPPET_URI));
        verify(readSnippetUseCase).readSnippet(SNIPPET_URI);
    }

    @Test
    @DisplayName("shows the content of the loaded snippet")
    void setSnippetTitle() {
        eventBus.post(new SnippetSelectedEvent(SNIPPET_URI));

        verify(snippetTitleController).setText(testSnippet.getTitle());
        verify(snippetDescriptionController).setText(testSnippet.getDescription());
        verify(snippetCodeController).setText(testSnippet.getCode());
        verify(snippetCodeController).setLanguage(testSnippet.getLanguage());
        verify(snippetDetailsController).setTags(testSnippet.getTags());
        verify(snippetFooterController).setPermissions(testSnippet.getPermissions());
    }

    @Test
    @DisplayName("clears all values when a CreateSnippetEvent is triggered")
    void newSnippet() {
        eventBus.post(new CreateSnippetEvent());

        verify(snippetTitleController).setText("");
        verify(snippetDescriptionController).setText("");
        verify(snippetCodeController).setText("");
        verify(snippetCodeController).setLanguage(null);
        verify(snippetDetailsController).setTags(Collections.emptyList());
        verify(snippetFooterController).setPermissions(Collections.emptySet());
    }

    @Test
    @DisplayName("deletes the current snippet when the delete-button is pressed")
    void deleteSnippet() throws Exception {
        callInitialize();
        eventBus.post(new SnippetSelectedEvent(SNIPPET_URI));

        snippetFooterController.delete();
        verify(deleteSnippetUseCase).deleteSnippet(SNIPPET_URI);
    }

    @Nested
    @DisplayName("when entering a new snippet")
    class NewSnippetTest {
        private final NewSnippetDto newSnippet = new NewSnippetDto(
                "A new snippet",
                "A description",
                new Language("Python", "1"),
                "print(\"Hello, World!\");",
                List.of("python", "test")
        );

        @Test
        @DisplayName("creates a new snippet based on the input")
        void createSnippet() {
            when(snippetTitleController.getText()).thenReturn(newSnippet.title());
            when(snippetDescriptionController.getText()).thenReturn(newSnippet.description());
            when(snippetCodeController.getLanguage()).thenReturn(newSnippet.language());
            when(snippetCodeController.getText()).thenReturn(newSnippet.code());
            when(snippetDetailsController.getTags()).thenReturn(newSnippet.tags());

            Snippet createdSnippet = new SnippetBuilder().uri(SNIPPET_URI)
                                                         .title(newSnippet.title())
                                                         .description(newSnippet.description())
                                                         .code(newSnippet.code())
                                                         .language(newSnippet.language())
                                                         .tags(newSnippet.tags())
                                                         .permissions(Set.of(Permission.DELETE))
                                                         .build();

            when(createSnippetUseCase.create(newSnippet)).thenReturn(createdSnippet);

            eventBus.post(new CreateSnippetEvent());
            snippetFooterController.save();

            verify(snippetTitleController).setText(createdSnippet.getTitle());
            verify(snippetDescriptionController).setText(createdSnippet.getDescription());
            verify(snippetCodeController).setText(createdSnippet.getCode());
            verify(snippetCodeController).setLanguage(createdSnippet.getLanguage());
            verify(snippetDetailsController).setTags(createdSnippet.getTags());
            verify(snippetFooterController).setPermissions(createdSnippet.getPermissions());
        }

        @Test
        @DisplayName("clears the input when the input is canceled")
        void clearInput() {
            eventBus.post(new CreateSnippetEvent());
            snippetFooterController.cancel();

            var twoTimes = times(2);
            verify(snippetTitleController, twoTimes).setText("");
            verify(snippetDescriptionController, twoTimes).setText("");
            verify(snippetCodeController, twoTimes).setText("");
            verify(snippetCodeController, twoTimes).setLanguage(null);
            verify(snippetDetailsController, twoTimes).setTags(Collections.emptyList());
            verify(snippetFooterController, twoTimes).setPermissions(Collections.emptySet());
        }
    }

    private void callInitialize() throws Exception {
        Method method = SnippetController.class.getDeclaredMethod("initialize");
        method.setAccessible(true);
        method.invoke(snippetController);
    }

    private static class TestFooter extends SnippetFooter {
        private Runnable onSave;
        private Runnable onCancel;
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
        public void onDelete(Runnable callback) {
            onDelete = callback;
        }

        void save() {
            onSave.run();
        }

        void cancel() {
            onCancel.run();
        }

        void delete() {
            onDelete.run();
        }

        @Override
        public void setPermissions(@Nonnull Set<Permission> permissions) {}

        @Override
        public void bindEditing(BooleanProperty editingProperty) {}
    }
}