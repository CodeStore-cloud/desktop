package cloud.codestore.client.ui.snippet;

import cloud.codestore.client.Snippet;
import cloud.codestore.client.SnippetBuilder;
import cloud.codestore.client.ui.selection.list.SnippetSelectedEvent;
import cloud.codestore.client.ui.snippet.description.SnippetDescription;
import cloud.codestore.client.ui.snippet.title.SnippetTitle;
import cloud.codestore.client.usecases.readsnippet.ReadSnippet;
import com.google.common.eventbus.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationExtension;

import static org.apache.commons.lang3.reflect.FieldUtils.writeField;
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
    @DisplayName("shows the title of the loaded snippet")
    void setSnippetTitle() {
        eventBus.post(new SnippetSelectedEvent(SNIPPET_URI));
        verify(snippetTitleController).setText("A random title");
    }

    private Snippet testSnippet() {
        return new SnippetBuilder().uri(SNIPPET_URI)
                                   .title("A random title")
                                   .build();
    }

    private void injectFxmlControllers() throws IllegalAccessException {
        writeField(snippetController, "snippetTitleController", snippetTitleController, true);
        writeField(snippetController, "snippetDescriptionController", snippetDescriptionController, true);
    }
}