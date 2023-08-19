package cloud.codestore.client.repositories.snippets;

import cloud.codestore.client.Snippet;
import cloud.codestore.client.repositories.HttpClient;
import cloud.codestore.jsonapi.document.ResourceCollectionDocument;
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

    @Mock
    private HttpClient client;
    private LocalSnippetRepository repository;

    @BeforeEach
    void setUp() {
        repository = new LocalSnippetRepository(client);
        when(client.getSnippetCollectionUrl()).thenReturn(SNIPPETS_URL);
    }

    @Test
    @DisplayName("retrieves all available code snippets from the core")
    void retrieveSnippetList() {
        SnippetResource[] testSnippets = testSnippets();
        var resourceCollection = new ResourceCollectionDocument<>(testSnippets);
        when(client.getCollection(SNIPPETS_URL, SnippetResource.class)).thenReturn(resourceCollection);

        List<Snippet> snippets = repository.get();

        assertThat(snippets).isNotNull().isNotEmpty().hasSameSizeAs(testSnippets);
        for (Snippet snippet : snippets) {
            assertThat(snippet.getUri()).isNotEmpty();
            assertThat(snippet.getTitle()).isNotNull().isNotEmpty();
        }

        verify(client).getCollection(SNIPPETS_URL, SnippetResource.class);
    }

    private SnippetResource[] testSnippets() {
        return new SnippetResource[] {
                testSnippet(1, "A hello-world example"),
                testSnippet(2, "JUnit5 cheatsheet"),
                testSnippet(3, "Another test snippet")
        };
    }

    private SnippetResource testSnippet(int id, String title) {
        SnippetResource snippet = mock(SnippetResource.class);
        when(snippet.getSelfLink()).thenReturn(SNIPPETS_URL + "/" + id);
        when(snippet.getTitle()).thenReturn(title);
        return snippet;
    }
}