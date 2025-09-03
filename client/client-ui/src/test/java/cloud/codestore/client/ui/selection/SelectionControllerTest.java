package cloud.codestore.client.ui.selection;

import cloud.codestore.client.ui.AbstractUiTest;
import cloud.codestore.client.ui.JavaFxInitializationExtension;
import cloud.codestore.client.ui.selection.filter.Filter;
import cloud.codestore.client.ui.selection.list.SnippetList;
import cloud.codestore.client.ui.selection.search.FullTextSearch;
import cloud.codestore.client.ui.selection.sort.Sort;
import cloud.codestore.client.usecases.listsnippets.FilterProperties;
import cloud.codestore.client.usecases.listsnippets.SortProperties;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static cloud.codestore.client.usecases.listsnippets.SortProperties.SnippetProperty.RELEVANCE;
import static cloud.codestore.client.usecases.listsnippets.SortProperties.SnippetProperty.TITLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, JavaFxInitializationExtension.class})
@DisplayName("The selection controller")
class SelectionControllerTest {
    @Spy
    private Button filterButton = new Button();
    @Mock
    private FullTextSearch searchController;
    @Mock
    private Filter filterController;
    @Mock
    private Sort sortController;
    @Mock
    private SnippetList snippetListController;
    @InjectMocks
    private SelectionController selectionController = new SelectionController();

    private StringProperty searchInputProperty = new SimpleStringProperty();
    private ObjectProperty<SortProperties> sortProperties = new SimpleObjectProperty<>();
    private ObjectProperty<FilterProperties> filterProperties = new SimpleObjectProperty<>();
    private final Map<KeyCode, Runnable> keyCodeHandlers = new HashMap<>();

    @BeforeEach
    void setUp() throws Exception {
        when(searchController.inputProperty()).thenReturn(searchInputProperty);
        when(filterController.filterProperties()).thenReturn(filterProperties);
        when(sortController.sortProperties()).thenReturn(sortProperties);

        doAnswer(invocation -> {
            keyCodeHandlers.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(searchController).registerKeyHandler(any(KeyCode.class), any(Runnable.class));

        AbstractUiTest.callInitialize(selectionController);
    }

    @Test
    @DisplayName("selects relevance when a search term is entered")
    void relevanceSelection() {
        assertThat(sortProperties.get()).isNull();
        searchInputProperty.set("Hello World");
        assertThat(sortProperties.get()).isEqualTo(new SortProperties(RELEVANCE, true));
    }

    @Test
    @DisplayName("selects previous option when the search is cleared")
    void defaultNoSearch() {
        var previousValue = new SortProperties(TITLE, true);
        sortProperties.set(previousValue);

        searchInputProperty.set("Hello World");
        assertThat(sortProperties.get()).isEqualTo(new SortProperties(RELEVANCE, true));
        searchInputProperty.set("");
        assertThat(sortProperties.get()).isEqualTo(previousValue);
    }

    @Test
    @DisplayName("updates the style of the filter button")
    void updateFilterButtonStyle() {
        ObservableList<String> styleClass = FXCollections.observableArrayList();
        when(filterButton.getStyleClass()).thenReturn(styleClass);

        filterProperties.set(new FilterProperties(Set.of("tag"), null));
        assertThat(styleClass).contains("filled");

        filterProperties.set(new FilterProperties());
        assertThat(styleClass).isEmpty();
    }

    @Test
    @DisplayName("selects the next code snippet when 'DOWN' is pressed")
    void selectNextSnippet() {
        keyCodeHandlers.get(KeyCode.DOWN).run();
        verify(snippetListController).selectNextSnippet();
    }

    @Test
    @DisplayName("selects the next code snippet when 'UP' is pressed")
    void selectPreviousSnippet() {
        keyCodeHandlers.get(KeyCode.UP).run();
        verify(snippetListController).selectPreviousSnippet();
    }

    @Nested
    @DisplayName("updates the snippet list")
    class UpdateSnippetList {
        @Test
        @DisplayName("when the search was changed")
        void updateSnippetListOnSearch() {
            searchInputProperty.set("Hello World");
            assertSnippetListUpdated();
        }

        @Test
        @DisplayName("when the sorting was changed")
        void updateSnippetListOnSort() {
            sortProperties.set(new SortProperties(TITLE, true));
            assertSnippetListUpdated();
        }

        @Test
        @DisplayName("when a filter was changed")
        void updateSnippetListOnFilter() {
            filterProperties.set(new FilterProperties(Set.of("tag1"), null));
            assertSnippetListUpdated();
        }

        private void assertSnippetListUpdated() {
            verify(snippetListController).update(any(), any(), any());
        }
    }
}