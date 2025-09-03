package cloud.codestore.client.ui;

import cloud.codestore.client.ui.selection.SelectionController;
import cloud.codestore.client.ui.snippet.SnippetController;
import com.google.common.eventbus.EventBus;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationTest;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("The root controller")
class RootControllerTest extends ApplicationTest {
    @Spy
    private Pane root = new Pane();
    @Mock
    private SelectionController selectionController;
    @Mock
    private SnippetController snippetController;
    private EventBus eventBus = new EventBus();
    @InjectMocks
    private RootController rootController = new RootController(eventBus);

    @BeforeEach
    void setUp() throws Exception {
        AbstractUiTest.callInitialize(rootController);
    }

    @Test
    @DisplayName("initially loads the snippet list when the application has been started")
    void updateSnippetListOnAppStart() {
        eventBus.post(new ApplicationReadyEvent());
        verify(selectionController).reloadSnippets();
    }

    @Test
    @DisplayName("reloads the snippet list when receiving an SnippetsChangedEvent")
    void reloadSnippetsOnChange() {
        root.fireEvent(new SnippetsChangedEvent(SnippetsChangedEvent.SNIPPET_CREATED));
        verify(selectionController).reloadSnippets();
    }

    @Test
    @DisplayName("requests snippet creation when receiving the corresponding ChangeSnippetsEvent.")
    void createSnippet() {
        root.fireEvent(new ChangeSnippetsEvent(ChangeSnippetsEvent.CREATE_SNIPPET));
        verify(snippetController).createSnippet();
    }
}