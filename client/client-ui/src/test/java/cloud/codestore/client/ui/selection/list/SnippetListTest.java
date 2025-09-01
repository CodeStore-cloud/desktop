package cloud.codestore.client.ui.selection.list;

import cloud.codestore.client.Permission;
import cloud.codestore.client.ui.AbstractUiTest;
import cloud.codestore.client.usecases.listsnippets.ReadSnippetsUseCase;
import cloud.codestore.client.usecases.listsnippets.SnippetListItem;
import cloud.codestore.client.usecases.listsnippets.SnippetPage;
import com.google.common.eventbus.EventBus;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.Start;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testfx.assertions.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("The snippet list")
class SnippetListTest extends AbstractUiTest {
    private static final String SNIPPET_URI = uri(1);
    private static final String NEXT_PAGE_URL = "http://localhost:8080/snippets?page[number]=2";

    @Mock
    private ReadSnippetsUseCase readSnippetsUseCase;
    @Spy
    private EventBus eventBus = new EventBus();
    private SnippetList controller;

    @Start
    public void start(Stage stage) throws Exception {
        var firstPage = new SnippetPage(testItems(), NEXT_PAGE_URL, Collections.emptySet());
        lenient().when(readSnippetsUseCase.getPage(any(), any(), any())).thenReturn(firstPage);

        controller = new SnippetList(readSnippetsUseCase, eventBus);
        start(stage, "snippetList.fxml", controller);
        updateList();
    }

    @Test
    @DisplayName("shows items provided by the list-snippets use case")
    void showSnippets() {
        var items = listView().getItems();
        assertThat(items).containsExactlyInAnyOrder(testItems().toArray(new SnippetListItem[0]));
    }

    @Nested
    @DisplayName("when a snippet is selected")
    class SnippetSelected {
        private static final String PREVIOUSLY_SELECTED_SNIPPET_URI = uri(5);

        @BeforeEach
        void setUp() {
            initSelection(PREVIOUSLY_SELECTED_SNIPPET_URI);
        }

        @Test
        @DisplayName("sends RequestSnippetSelectionEvent but keeps the current selection")
        void selectSnippet() {
            interact(() -> listView().getSelectionModel().selectFirst());
            verify(eventBus).post(new RequestSnippetSelectionEvent(SNIPPET_URI));
            assertSelected(PREVIOUSLY_SELECTED_SNIPPET_URI);
        }

        @Test
        @DisplayName("ignores already selected snippets")
        void duplicateSelection() {
            initSelection(SNIPPET_URI);
            clearInvocations(eventBus);

            interact(() -> listView().getSelectionModel().selectFirst());
            verify(eventBus, never()).post(any(RequestSnippetSelectionEvent.class));
        }

        @Test
        @DisplayName("applies the selection when confirmed via a SnippetSelectedEvent")
        void snippetSelectionConfirmed() {
            eventBus.post(new SnippetSelectedEvent(SNIPPET_URI));
            assertSelected(SNIPPET_URI);
        }
    }

    @Test
    @DisplayName("fires a CreateSnippetEvent if the create-snippet button was clicked")
    void createSnippetEvent() {
        Button createSnippetButton = createSnippetButton();
        createSnippetButton.setVisible(true);
        clickOn(createSnippetButton);
        verify(eventBus).post(new CreateSnippetEvent());
    }

    @Test
    @DisplayName("shows a next-page button if more results are available")
    void showOrHidePaginationButton() {
        var page2 = page(Collections.emptyList());
        when(readSnippetsUseCase.getPage(NEXT_PAGE_URL)).thenReturn(page2);

        var showMoreButton = showMoreButton();
        assertThat(showMoreButton.isVisible()).isTrue();
        clickOn(showMoreButton);
        assertThat(showMoreButton.isVisible()).isFalse();
    }

    @Test
    @DisplayName("loads the next page of snippets when pressing \"show more\"")
    void loadNextPage() {
        var page2 = page(page2TestItems());
        when(readSnippetsUseCase.getPage(NEXT_PAGE_URL)).thenReturn(page2);
        var listView = listView();
        assertThat(listView.getItems()).hasSize(10);

        var showMoreButton = showMoreButton();
        clickOn(showMoreButton);

        assertThat(listView.getItems()).hasSize(20);
    }

    @Test
    @DisplayName("keeps the current selection when an update is requested")
    void keepSelection() {
        initSelection(SNIPPET_URI);
        updateList();
        assertSelected(SNIPPET_URI);
    }

    @Test
    @DisplayName("shows the create-snippet button if permitted")
    void createSnippetButtonVisibility() {
        when(readSnippetsUseCase.getPage(any(), any(), any()))
                .thenReturn(new SnippetPage(Collections.emptyList(), "", Set.of(Permission.CREATE)));

        updateList();
        assertThat(createSnippetButton()).isVisible();
    }

    @Test
    @DisplayName("selects the next snippet")
    void selectNextSnippet() {
        initSelection(uri(1));
        controller.selectNextSnippet();
        verify(eventBus).post(new RequestSnippetSelectionEvent(uri(2)));

        clearInvocations(eventBus);
        initSelection(uri(10));

        controller.selectNextSnippet();
        verify(eventBus, never()).post(new RequestSnippetSelectionEvent(anyString()));
    }

    @Test
    @DisplayName("selects the previous snippet")
    void selectPreviousSnippet() {
        initSelection(uri(5));
        controller.selectPreviousSnippet();
        verify(eventBus).post(new RequestSnippetSelectionEvent(uri(4)));

        clearInvocations(eventBus);
        initSelection(uri(1));

        controller.selectPreviousSnippet();
        verify(eventBus, never()).post(new RequestSnippetSelectionEvent(anyString()));
    }

    private void initSelection(String uri) {
        eventBus.post(new SnippetSelectedEvent(uri));
        assertSelected(uri);
    }

    private void assertSelected(String uri) {
        SnippetListItem selectedItem = listView().getSelectionModel().getSelectedItem();
        assertThat(selectedItem).isNotNull();
        assertThat(selectedItem.uri()).isEqualTo(uri);
    }

    private ListView<SnippetListItem> listView() {
        return lookup("#list").queryListView();
    }

    private Node showMoreButton() {
        return lookup("#nextPage").queryLabeled();
    }

    private Button createSnippetButton() {
        Button button = lookup("#createSnippet").queryButton();
        button.setPrefSize(32, 32);
        return button;
    }

    private static List<SnippetListItem> testItems() {
        return List.of(
                new SnippetListItem(uri(1), "Snippet test #1"),
                new SnippetListItem(uri(2), "Snippet test #2"),
                new SnippetListItem(uri(3), "Snippet test #3"),
                new SnippetListItem(uri(4), "Snippet test #4"),
                new SnippetListItem(uri(5), "Snippet test #5"),
                new SnippetListItem(uri(6), "Snippet test #6"),
                new SnippetListItem(uri(7), "Snippet test #7"),
                new SnippetListItem(uri(8), "Snippet test #8"),
                new SnippetListItem(uri(9), "Snippet test #9"),
                new SnippetListItem(uri(10), "Snippet test #10")
        );
    }

    private static List<SnippetListItem> page2TestItems() {
        return List.of(
                new SnippetListItem(uri(11), "Snippet test #11"),
                new SnippetListItem(uri(12), "Snippet test #12"),
                new SnippetListItem(uri(13), "Snippet test #13"),
                new SnippetListItem(uri(14), "Snippet test #14"),
                new SnippetListItem(uri(15), "Snippet test #15"),
                new SnippetListItem(uri(16), "Snippet test #16"),
                new SnippetListItem(uri(17), "Snippet test #17"),
                new SnippetListItem(uri(18), "Snippet test #18"),
                new SnippetListItem(uri(19), "Snippet test #19"),
                new SnippetListItem(uri(20), "Snippet test #20")
        );
    }

    private SnippetPage page(List<SnippetListItem> items) {
        return new SnippetPage(items, "", Collections.emptySet());
    }

    private static String uri(int snippetId) {
        return "http://localhost:8080/snippets/" + snippetId;
    }

    private void updateList() {
        interact(() -> controller.update(null, null, null));
    }
}