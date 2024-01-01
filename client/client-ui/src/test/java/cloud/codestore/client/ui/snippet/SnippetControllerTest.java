package cloud.codestore.client.ui.snippet;

import cloud.codestore.client.Permission;
import cloud.codestore.client.Snippet;
import cloud.codestore.client.ui.selection.list.SnippetSelectedEvent;
import cloud.codestore.client.ui.snippet.code.SnippetCode;
import cloud.codestore.client.ui.snippet.description.SnippetDescription;
import cloud.codestore.client.ui.snippet.details.SnippetDetails;
import cloud.codestore.client.ui.snippet.footer.Footer;
import cloud.codestore.client.ui.snippet.title.SnippetTitle;
import cloud.codestore.client.usecases.deletesnippet.DeleteSnippetUseCase;
import cloud.codestore.client.usecases.readsnippet.ReadSnippetUseCase;
import com.google.common.eventbus.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("The snippet controller")
class SnippetControllerTest {
    private static final String SNIPPET_URI = "http://localhost:8080/snippets/1";

    @Mock
    private ReadSnippetUseCase readSnippetUseCase;
    @Mock
    private DeleteSnippetUseCase deleteSnippetUseCase;
    private EventBus eventBus = new EventBus();

    @Mock
    private SnippetTitle snippetTitleController;
    @Mock
    private SnippetDescription snippetDescriptionController;
    @Mock
    private SnippetCode snippetCodeController;
    @Mock
    private SnippetDetails snippetDetailsController;
    @Mock
    private Footer snippetFooterController;

    @InjectMocks
    private SnippetController snippetController = new SnippetController(readSnippetUseCase, deleteSnippetUseCase, eventBus);

    @BeforeEach
    void setUp() {
        lenient().when(readSnippetUseCase.readSnippet(anyString())).thenReturn(testSnippet());
    }

    @Test
    @DisplayName("loads a code snippet when selected")
    void loadSnippet() {
        eventBus.post(new SnippetSelectedEvent(SNIPPET_URI));
        verify(readSnippetUseCase).readSnippet(SNIPPET_URI);
    }

    @Test
    @DisplayName("shows the content of the loaded snippet")
    @SuppressWarnings("unchecked")
    void setSnippetTitle() {
        ArgumentCaptor<List<String>> tagsArgument = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Set<Permission>> permissionsArgument = ArgumentCaptor.forClass(Set.class);

        eventBus.post(new SnippetSelectedEvent(SNIPPET_URI));

        verify(snippetTitleController).setText("A random title");
        verify(snippetDescriptionController).setText("With a short description");
        verify(snippetCodeController).setText("System.out.println(\"Hello, World!\");");
        verify(snippetDetailsController).setTags(tagsArgument.capture());
        assertThat(tagsArgument.getValue()).containsExactlyInAnyOrder("hello", "world");
        verify(snippetFooterController).setPermissions(permissionsArgument.capture());
        assertThat(permissionsArgument.getValue()).containsExactlyInAnyOrder(Permission.DELETE);
    }

    @Test
    @DisplayName("registers a callback for the delete-button")
    void deleteSnippet() throws Exception {
        callInitialize();
        verify(snippetFooterController).onDelete(any(Runnable.class));
    }

    private Snippet testSnippet() {
        return Snippet.builder()
                      .uri(SNIPPET_URI)
                      .title("A random title")
                      .description("With a short description")
                      .code("System.out.println(\"Hello, World!\");")
                      .tags(List.of("hello", "world"))
                      .permissions(Set.of(Permission.DELETE))
                      .build();
    }

    private void callInitialize() throws Exception {
        Method method = SnippetController.class.getDeclaredMethod("initialize");
        method.setAccessible(true);
        method.invoke(snippetController);
    }
}