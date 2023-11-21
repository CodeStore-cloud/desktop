package cloud.codestore.client.ui.snippet;

import cloud.codestore.client.Snippet;
import cloud.codestore.client.ui.selection.list.SnippetSelectedEvent;
import cloud.codestore.client.ui.snippet.code.SnippetCode;
import cloud.codestore.client.ui.snippet.description.SnippetDescription;
import cloud.codestore.client.ui.snippet.details.SnippetDetails;
import cloud.codestore.client.ui.snippet.title.SnippetTitle;
import cloud.codestore.client.usecases.readsnippet.ReadSnippet;
import com.google.common.eventbus.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationExtension;

import java.util.List;

import static org.apache.commons.lang3.reflect.FieldUtils.writeField;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, ApplicationExtension.class})
@DisplayName("The snippet controller")
class SnippetControllerTest {
    private static final String SNIPPET_URI = "http://localhost:8080/snippets/1";

    @Mock
    private ReadSnippet readSnippetUseCase;
    @Mock
    private SnippetTitle snippetTitleController;
    @Mock
    private SnippetDescription snippetDescriptionController;
    @Mock
    private SnippetCode snippetCodeController;
    @Mock
    private SnippetDetails snippetDetailsController;

    private EventBus eventBus = new EventBus();
    private SnippetController snippetController;

    @BeforeEach
    void setUp() throws IllegalAccessException {
        when(readSnippetUseCase.readSnippet(anyString())).thenReturn(testSnippet());
        snippetController = new SnippetController(readSnippetUseCase, eventBus);
        injectFxmlControllers();
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

        eventBus.post(new SnippetSelectedEvent(SNIPPET_URI));

        verify(snippetTitleController).setText("A random title");
        verify(snippetDescriptionController).setText("With a short description");
        verify(snippetCodeController).setText("System.out.println(\"Hello, World!\");");
        verify(snippetDetailsController).setTags(tagsArgument.capture());
        assertThat(tagsArgument.getValue()).containsExactlyInAnyOrder("hello", "world");
    }

    private Snippet testSnippet() {
        return Snippet.builder()
                      .uri(SNIPPET_URI)
                      .title("A random title")
                      .description("With a short description")
                      .code("System.out.println(\"Hello, World!\");")
                      .tags(List.of("hello", "world"))
                      .build();
    }

    private void injectFxmlControllers() throws IllegalAccessException {
        writeField(snippetController, "snippetTitleController", snippetTitleController, true);
        writeField(snippetController, "snippetDescriptionController", snippetDescriptionController, true);
        writeField(snippetController, "snippetCodeController", snippetCodeController, true);
        writeField(snippetController, "snippetDetailsController", snippetDetailsController, true);
    }
}