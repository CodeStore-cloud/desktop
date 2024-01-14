package cloud.codestore.client.ui.selection.list;

import cloud.codestore.client.Permission;
import cloud.codestore.client.ui.AbstractUiTest;
import cloud.codestore.client.ui.selection.filter.FilterEvent;
import cloud.codestore.client.ui.selection.search.FullTextSearchEvent;
import cloud.codestore.client.ui.selection.sort.SortEvent;
import cloud.codestore.client.ui.snippet.SnippetCreatedEvent;
import cloud.codestore.client.ui.snippet.SnippetDeletedEvent;
import cloud.codestore.client.ui.snippet.SnippetUpdatedEvent;
import cloud.codestore.client.usecases.listsnippets.*;
import com.google.common.eventbus.EventBus;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.Start;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.testfx.assertions.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("The snippet list")
class SnippetListTest extends AbstractUiTest {
    private static final String SNIPPET_URI = "http://localhost:8080/snippets/1";
    private static final String NEXT_PAGE_URL = "http://localhost:8080/snippets?page[number]=2";

    @Mock
    private ReadSnippetsUseCase readSnippetsUseCase;
    @Spy
    private EventBus eventBus = new EventBus();

    @Start
    public void start(Stage stage) throws Exception {
        var firstPage = new SnippetPage(testItems(), NEXT_PAGE_URL, Collections.emptySet());
        when(readSnippetsUseCase.getPage(anyString(), any(), any())).thenReturn(firstPage);

        SnippetList controller = new SnippetList(readSnippetsUseCase, eventBus);
        start(stage, "snippetList.fxml", controller);
    }

    @Test
    @DisplayName("shows items provided by the list-snippets use case")
    void showSnippets() {
        var items = listView().getItems();
        assertThat(items).containsExactlyInAnyOrder(testItems().toArray(new SnippetListItem[0]));
    }

    @Test
    @DisplayName("triggers a SnippetSelectedEvent when a snippet is selected")
    void selectSnippet() {
        var argument = ArgumentCaptor.forClass(SnippetSelectedEvent.class);

        listView().getSelectionModel().select(0);

        verify(eventBus).post(argument.capture());
        SnippetSelectedEvent event = argument.getValue();
        assertThat(event.snippetUri()).isEqualTo(uri(1));
    }

    @Test
    @DisplayName("triggers a CreateSnippetEvent if the create-snippet button was clicked")
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
    @DisplayName("reloads the snippets when a FullTextSearchEvent is triggered")
    void searchSnippets() {
        var page = page(reducedSnippetList());
        when(readSnippetsUseCase.getPage(eq("test"), any(), any())).thenReturn(page);

        var listView = listView();
        assertThat(listView.getItems()).hasSize(10);

        interact(() -> {
            eventBus.post(new FullTextSearchEvent("test"));
            assertThat(listView.getItems()).hasSize(3);
        });
    }

    @Test
    @DisplayName("reloads the snippets when a FilterEvent is triggered")
    void filterSnippets() {
        var filterProperties = new FilterProperties(Set.of("hello", "world"), null);
        var page = page(reducedSnippetList());
        when(readSnippetsUseCase.getPage(anyString(), eq(filterProperties), any())).thenReturn(page);

        var listView = listView();
        assertThat(listView.getItems()).hasSize(10);

        interact(() -> {
            eventBus.post(new FilterEvent(filterProperties));
            assertThat(listView.getItems()).hasSize(3);
        });
    }

    @Test
    @DisplayName("reloads the snippets when a SortEvent is triggered")
    void sortSnippets() {
        var sortProperties = new SortProperties(SortProperties.SnippetProperty.TITLE, true);
        interact(() -> eventBus.post(new SortEvent(sortProperties)));
        verify(readSnippetsUseCase).getPage(anyString(), any(), eq(sortProperties));
    }

    @Test
    @DisplayName("reloads the snippets when a SippetCreatedEvent is triggered")
    void snippetCreated() {
        reset(readSnippetsUseCase);
        interact(() -> eventBus.post(new SnippetCreatedEvent(SNIPPET_URI)));
        verify(readSnippetsUseCase).getPage(anyString(), any(), any());
    }

    @Test
    @DisplayName("reloads the snippets when a SnippetUpdatedEvent is triggered")
    void snippetUpdated() {
        reset(readSnippetsUseCase);
        interact(() -> eventBus.post(new SnippetUpdatedEvent(SNIPPET_URI)));
        verify(readSnippetsUseCase).getPage(anyString(), any(), any());
    }

    @Test
    @DisplayName("reloads the snippets when a SnippetDeletedEvent is triggered")
    void snippetDeleted() {
        reset(readSnippetsUseCase);
        interact(() -> eventBus.post(new SnippetDeletedEvent(SNIPPET_URI)));
        verify(readSnippetsUseCase).getPage(anyString(), any(), any());
    }

    @Test
    @DisplayName("shows the create-snippet button if permitted")
    void createSnippetButtonVisibility() {
        var button = createSnippetButton();
        assertThat(button.isVisible()).isFalse();

        when(readSnippetsUseCase.getPage(any(), any(), any()))
                .thenReturn(new SnippetPage(Collections.emptyList(), "", Set.of(Permission.CREATE)));

        interact(() -> {
            eventBus.post(new FullTextSearchEvent("test"));
            assertThat(button.isVisible()).isTrue();
        });
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

    private static List<SnippetListItem> reducedSnippetList() {
        return List.of(
                new SnippetListItem(uri(31), "Snippet test #31"),
                new SnippetListItem(uri(32), "Snippet test #32"),
                new SnippetListItem(uri(33), "Snippet test #33")
        );
    }

    private static String uri(int snippetId) {
        return "http://localhost:8080/snippets/" + snippetId;
    }
}