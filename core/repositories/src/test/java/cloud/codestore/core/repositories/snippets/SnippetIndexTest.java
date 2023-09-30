package cloud.codestore.core.repositories.snippets;

import cloud.codestore.core.Snippet;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

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

        Snippet snippet = testSnippet();
        index.add(snippet);

        List<String> searchResult = index.query(snippetById());
        assertThat(searchResult).hasSize(1);
        assertThat(searchResult.get(0)).isEqualTo(snippet.getId());
    }

    @Test
    @DisplayName("replaces existing snippets")
    void updateSnippet() {
        String originalTitle = "original";
        String newTitle = "updated";
        index.add(testSnippet(originalTitle));

        assertThat(index.query(snippetByTitle(originalTitle))).isNotEmpty();
        assertThat(index.query(snippetByTitle(newTitle))).isEmpty();

        index.update(testSnippet(newTitle));

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
        index.add(testSnippet());
        index.remove(SNIPPET_ID);
        assertThat(index.query(snippetById())).isEmpty();
    }

    private Query snippetById() {
        return new TermQuery(new Term(SnippetField.ID, SNIPPET_ID));
    }

    private Query snippetByTitle(String title) {
        return new TermQuery(new Term(SnippetField.TITLE, title.toLowerCase()));
    }

    private Snippet testSnippet() {
        return Snippet.builder().id(SNIPPET_ID).build();
    }

    private Snippet testSnippet(String title) {
        return Snippet.builder().id(SNIPPET_ID).title(title.toLowerCase()).build();
    }
}