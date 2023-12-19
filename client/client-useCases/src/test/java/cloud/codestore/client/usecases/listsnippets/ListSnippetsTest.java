package cloud.codestore.client.usecases.listsnippets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("The readSnippets-snippets use case")
class ListSnippetsTest {
    private static final String NEXT_PAGE_URL = "http://localhost:8080/snippets?page[number]=2";

    @Mock
    private ReadSnippetPageQuery query;
    private ListSnippets useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListSnippets(query);
    }

    @Test
    @DisplayName("returns a page of code snippets")
    void returnAllSnippets() {
        var expectedSnippets = allSnippets();
        var testPage = new SnippetPage(expectedSnippets, NEXT_PAGE_URL);
        when(query.getFirstPage()).thenReturn(testPage);

        var page = useCase.readSnippets();
        assertThat(page.snippets()).isSameAs(expectedSnippets);
        assertThat(page.nextPageUrl()).isEqualTo(NEXT_PAGE_URL);
    }

    private List<SnippetListItem> allSnippets() {
        return Map.of(
                          "localhost:8080/snippets/1", "Snippet #1",
                          "localhost:8080/snippets/2", "Snippet #2",
                          "localhost:8080/snippets/3", "Snippet #3",
                          "localhost:8080/snippets/4", "Snippet #4",
                          "localhost:8080/snippets/5", "Snippet #5"
                  )
                  .entrySet()
                  .stream()
                  .map(entry -> new SnippetListItem(entry.getKey(), entry.getValue()))
                  .toList();
    }
}