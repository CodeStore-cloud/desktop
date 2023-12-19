package cloud.codestore.client.repositories.snippets;

import cloud.codestore.client.Snippet;
import cloud.codestore.client.repositories.HttpClient;
import cloud.codestore.client.repositories.tags.LocalTagRepository;
import cloud.codestore.client.usecases.listsnippets.SnippetListItem;
import cloud.codestore.client.usecases.listsnippets.SnippetPage;
import cloud.codestore.jsonapi.document.ResourceCollectionDocument;
import cloud.codestore.jsonapi.document.SingleResourceDocument;
import cloud.codestore.jsonapi.link.Link;
import cloud.codestore.jsonapi.relationship.Relationship;
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
@DisplayName("The snippet repository")
class SnippetRepositoryTest {
    private static final String SNIPPETS_URL = "http://localhost:8080/snippets";
    private static final String SNIPPET_URI = "http://localhost:8080/snippets/1";
    private static final String TAGS_URI = "http://localhost:8080/tags?filter[snippet]=1";

    @Mock
    private HttpClient client;
    @Mock
    private LocalTagRepository tagRepository;
    private LocalSnippetRepository repository;

    @BeforeEach
    void setUp() {
        repository = new LocalSnippetRepository(client, tagRepository);
    }

    @Test
    @DisplayName("retrieves a page of code snippets from the core")
    void retrieveFirstPage() {
        SnippetResource[] testSnippets = testSnippets();
        var resourceCollection = new ResourceCollectionDocument<>(testSnippets);
        resourceCollection.getLinks().add(new Link(Link.NEXT, "/snippets?page[number]=2"));
        when(client.getSnippetCollectionUrl()).thenReturn(SNIPPETS_URL);
        when(client.getCollection(SNIPPETS_URL, SnippetResource.class)).thenReturn(resourceCollection);

        SnippetPage page = repository.getFirstPage();

        List<SnippetListItem> snippets = page.snippets();
        assertThat(snippets).isNotNull().isNotEmpty().hasSameSizeAs(testSnippets);
        for (SnippetListItem snippet : snippets) {
            assertThat(snippet.uri()).isNotEmpty();
            assertThat(snippet.title()).isNotNull().isNotEmpty();
        }

        assertThat(page.nextPageUrl()).isNotEmpty();
        verify(client).getCollection(SNIPPETS_URL, SnippetResource.class);
    }

    @Test
    @DisplayName("retrieves a single code snippet from the core")
    void retrieveSingleSnippet() {
        var document = new SingleResourceDocument<>(testSnippet(
                1,
                "A single snippet",
                "With a short description",
                "System.out.println(\"Hello, World!\");"
        ));
        when(client.get(SNIPPET_URI, SnippetResource.class)).thenReturn(document);
        when(tagRepository.get(TAGS_URI)).thenReturn(List.of("hello", "world"));

        Snippet snippet = repository.readSnippet(SNIPPET_URI);

        assertThat(snippet.getUri()).isEqualTo(SNIPPET_URI);
        assertThat(snippet.getTitle()).isEqualTo("A single snippet");
        assertThat(snippet.getDescription()).isEqualTo("With a short description");
        assertThat(snippet.getCode()).isEqualTo("System.out.println(\"Hello, World!\");");
        assertThat(snippet.getTags()).containsExactlyInAnyOrder("hello", "world");
    }

    private SnippetResource[] testSnippets() {
        return new SnippetResource[]{
                testSnippet(1, "A hello-world example"),
                testSnippet(2, "JUnit5 cheatsheet"),
                testSnippet(3, "Another test snippet")
        };
    }

    private SnippetResource testSnippet(int id, String title) {
        return testSnippet(id, title, "", "");
    }

    private SnippetResource testSnippet(int id, String title, String description, String code) {
        SnippetResource snippet = mock(SnippetResource.class);
        lenient().when(snippet.getSelfLink()).thenReturn(SNIPPETS_URL + "/" + id);
        lenient().when(snippet.getTitle()).thenReturn(title);
        lenient().when(snippet.getDescription()).thenReturn(description);
        lenient().when(snippet.getCode()).thenReturn(code);
        lenient().when(snippet.getTags()).thenReturn(new Relationship(TAGS_URI));
        return snippet;
    }
}