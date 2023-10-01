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
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("The filter-query builder")
class FilterQueryBuilderTest {
    @Test
    @DisplayName("returns an empty query if the filter properties are empty")
    void emptyQuery() {
        assertFilterProducesQuery(new FilterProperties(null), "");
    }

    @ParameterizedTest
    @MethodSource("langToQuery")
    @DisplayName("returns a query which contains a programming language")
    void filterByLanguage(Language language, String expectedQuery) {
        assertFilterProducesQuery(new FilterProperties(language), expectedQuery);
    }

    private static Stream<Arguments> langToQuery() {
        return Arrays.stream(Language.values())
                     .map(language -> {
                         String languageName = switch (language) {
                             case CLISP -> "lisp";
                             case SHELL -> "shell";
                             case BATCH -> "batch";
                             default -> language.toString().toLowerCase();
                         };
                         String expectedQuery = "+language:%s".formatted(languageName);
                         return Arguments.of(language, expectedQuery);
                     });
    }

    private void assertFilterProducesQuery(FilterProperties filterProperties, String expectedQuery) {
        Query query = new FilterQueryBuilder(filterProperties).buildFilterQuery();
        assertThat(query.toString()).isEqualTo(expectedQuery);
    }
}