package cloud.codestore.core.repositories.snippets;

import org.apache.lucene.search.Query;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("The search-query builder returns a query")
class SearchQueryBuilderTest {
    @Test
    @DisplayName("matching all snippets if the search term is empty")
    void emptySearchToken() {
        expectQuery("", "*:*");
    }

    @Test
    @DisplayName("matching snippets with a specific programming language")
    void searchForLanguage() {
        expectQuery("java", "(language:java)^1.5");
    }

    @Test
    @DisplayName("matching snippets that contain the term in the title")
    void searchForTitle() {
        expectQuery("query", "(title:query*)^1.5");
    }

    @Test
    @DisplayName("matching snippets that contain a specific tag")
    void searchForTags() {
        expectQuery("tagX", "(tag:tagX)^1.5");
    }

    @Test
    @DisplayName("matching snippets that contain the term in the description")
    void searchForDescription() {
        expectQuery("query", "description:query*");
    }

    @Test
    @DisplayName("matching snippets that contain the term in the code")
    void searchForCode() {
        expectQuery("query", "code:query*");
    }

    @Test
    @DisplayName("which respects multiple terms")
    void searchFoMultipleTerms() {
        expectQuery(
                "a b c",
                "(language:a)^1.5", "(language:b)^1.5", "(language:c)^1.5",
                "(title:a*)^1.5", "(title:b*)^1.5", "(title:c*)^1.5",
                "description:a*", "description:b*", "description:c*",
                "code:a*", "code:b*", "code:c*",
                "(tag:a)^1.5", "(tag:b)^1.5", "(tag:c)^1.5"
        );
    }

    private void expectQuery(String searchTerm, String... expectedQueryParts) {
        Query query = new SearchQueryBuilder(searchTerm).build();
        assertThat(query.toString()).contains(expectedQueryParts);
    }
}