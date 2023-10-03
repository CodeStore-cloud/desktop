package cloud.codestore.core.repositories.snippets;

import cloud.codestore.core.Language;
import cloud.codestore.core.Snippet;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static cloud.codestore.core.repositories.snippets.SnippetIndex.SnippetField;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("The snippet index")
class SnippetIndexTest {
    private static final String SNIPPET_ID = UUID.randomUUID().toString();

    private SnippetIndex index = new SnippetIndex();

    @Test
    @DisplayName("adds new snippets")
    void addSnippet() {
        assertThat(index.query(snippetById())).isEmpty();

        Snippet snippet = testSnippet(SNIPPET_ID);
        index.add(snippet);

        List<String> searchResult = index.query(snippetById()).toList();
        assertThat(searchResult).hasSize(1);
        assertThat(searchResult.get(0)).isEqualTo(snippet.getId());
    }

    @Test
    @DisplayName("adds multiple snippets at once")
    void addMultipleSnippets() {
        assertThat(index.query(new MatchAllDocsQuery())).isEmpty();
        index.add(Stream.of(testSnippet("1"), testSnippet("2"), testSnippet("3")));
        assertThat(index.query(new MatchAllDocsQuery())).hasSize(3);
    }

    @Test
    @DisplayName("replaces existing snippets")
    void updateSnippet() {
        String originalTitle = "original";
        String newTitle = "updated";
        index.add(snippetWithTitle(originalTitle));

        assertThat(index.query(snippetByTitle(originalTitle))).isNotEmpty();
        assertThat(index.query(snippetByTitle(newTitle))).isEmpty();

        index.update(snippetWithTitle(newTitle));

        assertThat(index.query(snippetByTitle(newTitle))).isNotEmpty();
        assertThat(index.query(snippetByTitle(originalTitle))).isEmpty();
    }

    @Test
    @DisplayName("returns an empty search result when empty")
    void emptySearchResult() {
        assertThat(index.query(snippetById())).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("removes existing snippets")
    void removeSnippet() {
        index.add(testSnippet(SNIPPET_ID));
        index.remove(SNIPPET_ID);
        assertThat(index.query(snippetById())).isEmpty();
    }

    @Nested
    @DisplayName("indexes the snippet's")
    class IndexSnippetPropertyTest {
        private Snippet snippt;

        @BeforeEach
        void setUp() {
            snippt = Snippet.builder().id(SNIPPET_ID)
                            .title("title")
                            .description("description")
                            .code("code")
                            .tags(List.of("tag"))
                            .language(Language.JAVA)
                            .build();

            assertThat(index.query(new MatchAllDocsQuery())).isEmpty();
            index.add(snippt);
        }

        @Test
        @DisplayName("title")
        void indexTitle() {
            assertThat(index.query(snippetByTitle("title"))).isNotEmpty();
        }

        @Test
        @DisplayName("description")
        void indexDescription() {
            assertThat(index.query(snippetByDescription("description"))).isNotEmpty();
        }

        @Test
        @DisplayName("code")
        void indexCode() {
            assertThat(index.query(snippetByCode("code"))).isNotEmpty();
        }

        @Test
        @DisplayName("tags")
        void indexTags() {
            assertThat(index.query(snippetByTag("tag"))).isNotEmpty();
        }

        @Test
        @DisplayName("language")
        void indexLanguage() {
            assertThat(index.query(snippetByLanguage(Language.JAVA))).isNotEmpty();
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

        private Query snippetByLanguage(Language language) {
            return new TermQuery(new Term(SnippetField.LANGUAGE, String.valueOf(language.getId())));
        }

        private Query snippetByTag(String tag) {
            return new TermQuery(new Term(SnippetField.TAG, tag));
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