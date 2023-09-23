package cloud.codestore.client.repositories.snippets;

import cloud.codestore.client.Snippet;
import cloud.codestore.client.repositories.HttpClient;
import cloud.codestore.client.usecases.listsnippets.SnippetListItem;
import cloud.codestore.jsonapi.document.ResourceCollectionDocument;
import cloud.codestore.jsonapi.document.SingleResourceDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("The local snippet repository")
class LocalSnippetRepositoryTest {
    private static final String SNIPPETS_URL = "http://localhost:8080/snippets";
    private static final String SNIPPET_URI = "http://localhost:8080/snippets/1";

    @Mock
    private HttpClient client;
    private LocalSnippetRepository repository;

    @BeforeEach
    void setUp() {
        repository = new LocalSnippetRepository(client);
    }

    @Test
    @DisplayName("retrieves all available code snippets from the core")
    void retrieveSnippetList() {
        SnippetResource[] testSnippets = testSnippets();
        var resourceCollection = new ResourceCollectionDocument<>(testSnippets);
        when(client.getCollection(SNIPPETS_URL, SnippetResource.class)).thenReturn(resourceCollection);
        when(client.getSnippetCollectionUrl()).thenReturn(SNIPPETS_URL);

        List<SnippetListItem> snippets = repository.get();

        assertThat(snippets).isNotNull().isNotEmpty().hasSameSizeAs(testSnippets);
        for (SnippetListItem snippet : snippets) {
            assertThat(snippet.uri()).isNotEmpty();
            assertThat(snippet.title()).isNotNull().isNotEmpty();
        }

        verify(client).getCollection(SNIPPETS_URL, SnippetResource.class);
    }

    @Test
    @DisplayName("retrieves a single code snippet from the core")
    void retrieveSingleSnippet() {
        var document = new SingleResourceDocument<>(testSnippet(1, "A single snippet", "With a short description"));
        when(client.get(SNIPPET_URI, SnippetResource.class)).thenReturn(document);

        Snippet snippet = repository.get(SNIPPET_URI);

        assertThat(snippet.getUri()).isEqualTo(SNIPPET_URI);
        assertThat(snippet.getTitle()).isEqualTo("A single snippet");
        assertThat(snippet.getDescription()).isEqualTo("With a short description");
    }

    private SnippetResource[] testSnippets() {
        return new SnippetResource[] {
                testSnippet(1, "A hello-world example"),
                testSnippet(2, "JUnit5 cheatsheet"),
                testSnippet(3, "Another test snippet")
        };
    }

    private SnippetResource testSnippet(int id, String title) {
        return testSnippet(id, title, "");
    }

    private SnippetResource testSnippet(int id, String title, String description) {
        SnippetResource snippet = mock(SnippetResource.class);
        when(snippet.getSelfLink()).thenReturn(SNIPPETS_URL + "/" + id);
        when(snippet.getTitle()).thenReturn(title);
        when(snippet.getDescription()).thenReturn(description);
        return snippet;
    }
}