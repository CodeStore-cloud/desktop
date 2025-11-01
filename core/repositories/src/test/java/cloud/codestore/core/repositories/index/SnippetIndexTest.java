package cloud.codestore.core.repositories.index;

import cloud.codestore.core.Language;
import cloud.codestore.core.Snippet;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.assertj.core.api.ListAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static cloud.codestore.core.repositories.index.SnippetIndex.SnippetField;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("The snippet index")
class SnippetIndexTest {
    private static final String SNIPPET_ID = UUID.randomUUID().toString();

    private SnippetIndex index = new SnippetIndex();
    private SortField defaultOrder = new SortField(SnippetField.CREATED, SortField.Type.LONG, true);

    @Test
    @DisplayName("adds new snippets")
    void addSnippet() {
        assertThat(index.query(snippetById(), defaultOrder)).isEmpty();

        Snippet snippet = testSnippet(SNIPPET_ID);
        index.add(snippet);

        List<String> searchResult = index.query(snippetById(), defaultOrder);
        assertThat(searchResult).hasSize(1);
        assertThat(searchResult.get(0)).isEqualTo(snippet.getId());
    }

    @Test
    @DisplayName("adds multiple snippets at once")
    void addMultipleSnippets() {
        assertThat(index.query(new MatchAllDocsQuery(), defaultOrder)).isEmpty();
        index.add(Stream.of(testSnippet("1"), testSnippet("2"), testSnippet("3")));
        assertThat(index.query(new MatchAllDocsQuery(), defaultOrder)).hasSize(3);
    }

    @Test
    @DisplayName("replaces existing snippets")
    void updateSnippet() {
        String originalTitle = "original";
        String newTitle = "updated";
        index.add(snippetWithTitle(originalTitle));

        assertThat(index.query(snippetByTitle(originalTitle), defaultOrder)).isNotEmpty();
        assertThat(index.query(snippetByTitle(newTitle), defaultOrder)).isEmpty();

        index.update(snippetWithTitle(newTitle));

        assertThat(index.query(snippetByTitle(newTitle), defaultOrder)).isNotEmpty();
        assertThat(index.query(snippetByTitle(originalTitle), defaultOrder)).isEmpty();
    }

    @Test
    @DisplayName("returns an empty search result when empty")
    void emptySearchResult() {
        assertThat(index.query(snippetById(), defaultOrder)).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("removes existing snippets")
    void removeSnippet() {
        index.add(testSnippet(SNIPPET_ID));
        index.remove(SNIPPET_ID);
        assertThat(index.query(snippetById(), defaultOrder)).isEmpty();
    }

    @Nested
    @DisplayName("indexes the snippet's")
    class IndexSnippetPropertyTest {
        private Snippet snippt;

        @BeforeEach
        void setUp() {
            snippt = Snippet.builder()
                            .id(SNIPPET_ID)
                            .title("title")
                            .description("description")
                            .code("code")
                            .tags(List.of("tag"))
                            .language(Language.JAVA)
                            .build();

            assertThat(index.query(new MatchAllDocsQuery(), defaultOrder)).isEmpty();
            index.add(snippt);
        }

        @Test
        @DisplayName("title")
        void indexTitle() {
            assertThat(index.query(snippetByTitle("title"), defaultOrder)).isNotEmpty();
        }

        @Test
        @DisplayName("description")
        void indexDescription() {
            assertThat(index.query(snippetByDescription("description"), defaultOrder)).isNotEmpty();
        }

        @Test
        @DisplayName("code")
        void indexCode() {
            assertThat(index.query(snippetByCode("code"), defaultOrder)).isNotEmpty();
        }

        @Test
        @DisplayName("tags")
        void indexTags() {
            assertThat(index.query(snippetByTag("tag"), defaultOrder)).isNotEmpty();
        }

        @Test
        @DisplayName("language id")
        void indexLanguageId() {
            String languageId = String.valueOf(Language.JAVA.getId());
            assertThat(index.query(snippetByLanguage(languageId), defaultOrder)).isNotEmpty();
        }

        @Test
        @DisplayName("language name")
        void indexLanguageName() {
            String languageName = Language.JAVA.getName().toLowerCase();
            assertThat(index.query(snippetByLanguage(languageName), defaultOrder)).isNotEmpty();
        }

        private Query snippetByTitle(String title) {
            return new TermQuery(new Term(SnippetField.TITLE, title));
        }

        private Query snippetByDescription(String description) {
            return new TermQuery(new Term(SnippetField.DESCRIPTION, description));
        }

        private Query snippetByCode(String code) {
            return new TermQuery(new Term(SnippetField.CODE, code));
        }

        private Query snippetByLanguage(String language) {
            return new TermQuery(new Term(SnippetField.LANGUAGE, language));
        }

        private Query snippetByTag(String tag) {
            return new TermQuery(new Term(SnippetField.TAG, tag));
        }
    }

    @Nested
    @DisplayName("can sort snippets by")
    class SortTest {
        @BeforeEach
        void setUp() {
            var now = OffsetDateTime.now(ZoneOffset.UTC);
            index.add(Snippet.builder()
                             .id("1")
                             .title("E")
                             .created(now.minusDays(5))
                             .modified(now.minusSeconds(5))
                             .build());
            index.add(Snippet.builder()
                             .id("2")
                             .title("D")
                             .created(now.minusHours(1))
                             .build());
            index.add(Snippet.builder()
                             .id("3")
                             .title("A")
                             .created(now.minusMonths(10))
                             .modified(now.minusHours(8))
                             .build());
            index.add(Snippet.builder()
                             .id("4")
                             .title("C")
                             .created(now.minusWeeks(1))
                             .build());
            index.add(Snippet.builder()
                             .id("5")
                             .title("B")
                             .created(now.minusMinutes(15))
                             .build());
        }

        @Test
        @DisplayName("creation time")
        void sortByCreationTime() {
            assertSortingBy(new SortField(SnippetField.CREATED, SortField.Type.LONG, true))
                    .containsExactly("5", "2", "1", "4", "3");
        }

        @Test
        @DisplayName("modification time")
        void sortByModificationTime() {
            assertSortingBy(new SortField(SnippetField.MODIFIED, SortField.Type.LONG, true))
                    .containsExactly("1", "5", "2", "3", "4");
        }

        @Test
        @DisplayName("title")
        void sortByTitle() {
            assertSortingBy(new SortField(SnippetField.TITLE, SortField.Type.STRING, false))
                    .containsExactly("3", "5", "4", "2", "1");
        }

        private ListAssert<String> assertSortingBy(SortField sortField) {
            return assertThat(index.query(new MatchAllDocsQuery(), sortField));
        }
    }

    private Snippet testSnippet(String id) {
        return Snippet.builder().id(id).build();
    }

    private Query snippetById() {
        return new TermQuery(new Term(SnippetField.ID, SNIPPET_ID));
    }

    private Query snippetByTitle(String title) {
        return new TermQuery(new Term(SnippetField.TITLE, title));
    }

    private Snippet snippetWithTitle(String title) {
        return Snippet.builder().id(SNIPPET_ID).title(title).build();
    }
}