package cloud.codestore.client.repositories.snippets;

import cloud.codestore.client.Language;
import cloud.codestore.client.Permission;
import cloud.codestore.client.Snippet;
import cloud.codestore.client.repositories.HttpClient;
import cloud.codestore.client.repositories.Operation;
import cloud.codestore.client.repositories.ResourceMetaInfo;
import cloud.codestore.client.repositories.language.LanguageResource;
import cloud.codestore.client.repositories.language.LocalLanguageRepository;
import cloud.codestore.client.repositories.tags.LocalTagRepository;
import cloud.codestore.client.repositories.tags.TagResource;
import cloud.codestore.client.usecases.createsnippet.NewSnippetDto;
import cloud.codestore.client.usecases.listsnippets.FilterProperties;
import cloud.codestore.client.usecases.listsnippets.SnippetListItem;
import cloud.codestore.client.usecases.listsnippets.SnippetPage;
import cloud.codestore.client.usecases.listsnippets.SortProperties;
import cloud.codestore.client.usecases.updatesnippet.UpdatedSnippetDto;
import cloud.codestore.jsonapi.document.ResourceCollectionDocument;
import cloud.codestore.jsonapi.document.SingleResourceDocument;
import cloud.codestore.jsonapi.link.Link;
import cloud.codestore.jsonapi.relationship.ToManyRelationship;
import cloud.codestore.jsonapi.relationship.ToOneRelationship;
import cloud.codestore.jsonapi.resource.ResourceIdentifierObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static cloud.codestore.client.usecases.listsnippets.SortProperties.SnippetProperty.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("The local snippet repository")
class LocalSnippetRepositoryTest {
    private static final String SNIPPETS_URL = "http://localhost:8080/snippets";
    private static final String SNIPPET_URI = "http://localhost:8080/snippets/1";
    private static final String SNIPPET_TAGS_URI = "http://localhost:8080/tags?filter[snippet]=1";
    private static final String TAGS_URI = "http://localhost:8080/tags";
    private static final String SNIPPET_LANGUAGE_URI = "http://localhost:8080/languages/10";

    @Mock
    private HttpClient client;
    @Mock
    private LocalTagRepository tagRepository;
    @Mock
    private LocalLanguageRepository languageRepository;
    @InjectMocks
    private LocalSnippetRepository repository;

    @BeforeEach
    void setUp() {
        lenient().when(client.getSnippetCollectionUrl()).thenReturn(SNIPPETS_URL);
    }

    @Test
    @DisplayName("retrieves a page of code snippets from the core")
    void retrieveFirstPage() {
        SnippetResource[] testSnippets = testSnippets();
        var resourceCollection = new ResourceCollectionDocument<>(testSnippets);
        resourceCollection.getLinks().add(new Link(Link.NEXT, "/snippets?page[number]=2"));
        when(client.getCollection(anyString(), eq(SnippetResource.class))).thenReturn(resourceCollection);

        SnippetPage page = repository.getPage("", new FilterProperties(), new SortProperties());

        List<SnippetListItem> snippets = page.snippets();
        assertThat(snippets).isNotNull().isNotEmpty().hasSameSizeAs(testSnippets);
        for (SnippetListItem snippet : snippets) {
            assertThat(snippet.uri()).isNotEmpty();
            assertThat(snippet.title()).isNotNull().isNotEmpty();
        }

        assertThat(page.nextPageUrl()).isNotEmpty();
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
        when(tagRepository.get(SNIPPET_TAGS_URI)).thenReturn(List.of("hello", "world"));
        Language java = new Language("Java", "10");
        when(languageRepository.get(SNIPPET_LANGUAGE_URI)).thenReturn(java);

        Snippet snippet = repository.readSnippet(SNIPPET_URI);

        assertThat(snippet.getId()).isEqualTo("1");
        assertThat(snippet.getUri()).isEqualTo(SNIPPET_URI);
        assertThat(snippet.getTitle()).isEqualTo("A single snippet");
        assertThat(snippet.getDescription()).isEqualTo("With a short description");
        assertThat(snippet.getCode()).isEqualTo("System.out.println(\"Hello, World!\");");
        assertThat(snippet.getTags()).containsExactlyInAnyOrder("hello", "world");
        assertThat(snippet.getLanguage()).isEqualTo(java);
    }

    @Test
    @DisplayName("reads the permissions of the snippet collection")
    void collectionPermissions() {
        var document = new ResourceCollectionDocument<>(testSnippets());
        document.setMeta(new ResourceMetaInfo(new Operation("createSnippet")));
        when(client.getCollection(anyString(), eq(SnippetResource.class))).thenReturn(document);

        SnippetPage page = repository.getPage("", new FilterProperties(), new SortProperties());

        assertThat(page.permissions()).containsExactly(Permission.CREATE);
    }

    @Test
    @DisplayName("reads the permissions of a single code snippet")
    void snippetPermissions() {
        var resource = testSnippet(1, "title", "", "Hello, World!");
        var document = new SingleResourceDocument<>(resource);
        document.setMeta(new ResourceMetaInfo(
                new Operation("deleteSnippet"),
                new Operation("updateSnippet")
        ));
        when(client.get(SNIPPET_URI, SnippetResource.class)).thenReturn(document);

        Snippet snippet = repository.readSnippet(SNIPPET_URI);

        assertThat(snippet.getPermissions()).containsExactlyInAnyOrder(Permission.DELETE, Permission.UPDATE);
    }

    @Test
    @DisplayName("passes the provided search query to the core")
    void searchSnippets() {
        var resourceCollection = new ResourceCollectionDocument<>(testSnippets());
        when(client.getCollection(anyString(), eq(SnippetResource.class))).thenReturn(resourceCollection);

        repository.getPage("test", new FilterProperties(), new SortProperties());
        verify(client).getCollection(argThat(url -> url.contains("searchQuery=test")), any());
    }

    @Test
    @DisplayName("passes the provided filter properties to the core")
    void filterSnippets() {
        var resourceCollection = new ResourceCollectionDocument<>(testSnippets());
        when(client.getCollection(anyString(), eq(SnippetResource.class))).thenReturn(resourceCollection);

        var tags = new TreeSet<>(Set.of("hello", "world"));
        var language = new Language("Java", "9");
        var filterProperties = new FilterProperties(tags, language);
        repository.getPage("", filterProperties, new SortProperties());

        verify(client).getCollection(
                argThat(url -> url.contains("filter%5Btags%5D=hello,world") && url.contains("filter%5Blanguage%5D=9")),
                any()
        );
    }

    @ParameterizedTest
    @MethodSource("sortParamStream")
    @DisplayName("passes the provided sort properties to the core")
    void sortSnippets(SortProperties sortProperties, String expectedSortParam) {
        var document = new ResourceCollectionDocument<>(testSnippets());
        lenient().when(client.getCollection(anyString(), eq(SnippetResource.class))).thenReturn(document);

        repository.getPage("", new FilterProperties(), sortProperties);
        verify(client).getCollection(argThat(url -> url.contains("sort=" + expectedSortParam)), any());
    }

    private static Stream<Arguments> sortParamStream() {
        return Stream.of(
                Arguments.of(new SortProperties(RELEVANCE, true), "relevance"),
                Arguments.of(new SortProperties(RELEVANCE, false), "-relevance"),
                Arguments.of(new SortProperties(CREATED, true), "created"),
                Arguments.of(new SortProperties(CREATED, false), "-created"),
                Arguments.of(new SortProperties(MODIFIED, true), "modified"),
                Arguments.of(new SortProperties(MODIFIED, false), "-modified"),
                Arguments.of(new SortProperties(TITLE, true), "title"),
                Arguments.of(new SortProperties(TITLE, false), "-title")
        );
    }

    @Test
    @DisplayName("creates a new code snippet in the core")
    void createSnippet() {
        TagResource tag1 = mock(TagResource.class);
        when(tag1.getIdentifier()).thenReturn(new ResourceIdentifierObject(TagResource.RESOURCE_TYPE, "1"));
        TagResource tag2 = mock(TagResource.class);
        when(tag2.getIdentifier()).thenReturn(new ResourceIdentifierObject(TagResource.RESOURCE_TYPE, "2"));
        when(client.getTagsCollectionUrl()).thenReturn(TAGS_URI);

        var doc1 = new SingleResourceDocument<>(tag1);
        var doc2 = new SingleResourceDocument<>(tag2);
        when(client.post(eq(TAGS_URI), any(TagResource.class))).thenReturn(doc1).thenReturn(doc2);

        NewSnippetDto dto = new NewSnippetDto(
                "A hello world example",
                "With a short description",
                new Language("Java", "10"),
                "System.out.println(\"Hello, World!\");",
                List.of("hello", "world")
        );
        var document = new SingleResourceDocument<>(testSnippet(1, "title"));
        when(client.post(eq(SNIPPETS_URL), any(SnippetResource.class))).thenReturn(document);

        var snippetResourceArgument = ArgumentCaptor.forClass(SnippetResource.class);

        repository.create(dto);

        verify(client, times(2)).post(eq(TAGS_URI), any(TagResource.class));
        verify(client).post(eq(SNIPPETS_URL), snippetResourceArgument.capture());

        SnippetResource snippetResource = snippetResourceArgument.getValue();
        assertThat(snippetResource.getTitle()).isEqualTo(dto.title());
        assertThat(snippetResource.getDescription()).isEqualTo(dto.description());
        assertThat(snippetResource.getCode()).isEqualTo(dto.code());
        assertThat(snippetResource.getLanguage()).isNotNull();
        assertThat(snippetResource.getLanguage().getData()).isEqualTo(
                new ResourceIdentifierObject(LanguageResource.RESOURCE_TYPE, "10"));
        assertThat(snippetResource.getTags()).isNotNull();
        assertThat(snippetResource.getTags().getData()).hasSize(2);
    }

    @Test
    @DisplayName("updates a code snippet in the core")
    void updateSnippet() {
        TagResource tag1 = mock(TagResource.class);
        when(tag1.getIdentifier()).thenReturn(new ResourceIdentifierObject(TagResource.RESOURCE_TYPE, "1"));
        TagResource tag2 = mock(TagResource.class);
        when(tag2.getIdentifier()).thenReturn(new ResourceIdentifierObject(TagResource.RESOURCE_TYPE, "2"));
        when(client.getTagsCollectionUrl()).thenReturn(TAGS_URI);

        var doc1 = new SingleResourceDocument<>(tag1);
        var doc2 = new SingleResourceDocument<>(tag2);
        when(client.post(eq(TAGS_URI), any(TagResource.class))).thenReturn(doc1).thenReturn(doc2);

        UpdatedSnippetDto dto = new UpdatedSnippetDto(
                "1",
                SNIPPET_URI,
                "An updated example",
                "With another description",
                new Language("Python", "9"),
                "print(\"Hello, World!\");",
                List.of("hello", "world")
        );

        var document = new SingleResourceDocument<>(testSnippet(1, "title"));
        when(client.patch(eq(SNIPPET_URI), any(SnippetResource.class))).thenReturn(document);

        var snippetResourceArgument = ArgumentCaptor.forClass(SnippetResource.class);

        repository.update(dto);

        verify(client, times(2)).post(eq(TAGS_URI), any(TagResource.class));
        verify(client).patch(eq(SNIPPET_URI), snippetResourceArgument.capture());

        SnippetResource snippetResource = snippetResourceArgument.getValue();
        assertThat(snippetResource.getTitle()).isEqualTo(dto.title());
        assertThat(snippetResource.getDescription()).isEqualTo(dto.description());
        assertThat(snippetResource.getCode()).isEqualTo(dto.code());
        assertThat(snippetResource.getLanguage()).isNotNull();
        assertThat(snippetResource.getLanguage().getData()).isEqualTo(
                new ResourceIdentifierObject(LanguageResource.RESOURCE_TYPE, "9"));
        assertThat(snippetResource.getTags()).isNotNull();
        assertThat(snippetResource.getTags().getData()).hasSize(2);
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
        SnippetResource snippet = spy(new SnippetResource(
                title, description, code,
                new ToOneRelationship<>(SNIPPET_LANGUAGE_URI),
                new ToManyRelationship<>(SNIPPET_TAGS_URI)
        ));

        lenient().when(snippet.getId()).thenReturn(String.valueOf(id));
        lenient().when(snippet.getSelfLink()).thenReturn(SNIPPETS_URL + "/" + id);

        return snippet;
    }
}