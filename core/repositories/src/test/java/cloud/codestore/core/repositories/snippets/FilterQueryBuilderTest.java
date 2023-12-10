package cloud.codestore.core.repositories.snippets;

import cloud.codestore.core.Language;
import cloud.codestore.core.usecases.listsnippets.FilterProperties;
import org.apache.lucene.search.Query;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("The filter-query builder returns a query")
class FilterQueryBuilderTest {
    @Test
    @DisplayName("matching all code snippets if the filter properties are empty")
    void emptyQuery() {
        expectQuery(new FilterProperties(), "*:*");
    }

    @ParameterizedTest
    @MethodSource("langToQuery")
    @DisplayName("which contains a programming language")
    void filterByLanguage(Language language, String expectedQuery) {
        expectQuery(new FilterProperties(language, null), expectedQuery);
    }

    private static Stream<Arguments> langToQuery() {
        return Arrays.stream(Language.values())
                     .map(language -> {
                         String expectedQuery = "+language:" + language.getId();
                         return Arguments.of(language, expectedQuery);
                     });
    }

    @Test
    @DisplayName("which contains the provided tags")
    void filterByLanguage() {
        Set<String> tags = Set.of("Tag-A", "Tag_B", "TagC");
        String[] expectedQuery = new String[]{"+tag:taga", "+tag:tagb", "+tag:tagc"};
        expectQuery(new FilterProperties(null, tags), expectedQuery);
    }

    private void expectQuery(FilterProperties filterProperties, String... expectedQueryParts) {
        Query query = new FilterQueryBuilder(filterProperties).build();
        assertThat(query.toString()).contains(expectedQueryParts);
    }
}