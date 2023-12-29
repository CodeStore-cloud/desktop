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
import java.util.Collections;
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
    @MethodSource("queryByLanguageId")
    @DisplayName("which contains the id of a programming language")
    void filterByLanguageId(int languageId, String expectedQuery) {
        expectQuery(new FilterProperties(String.valueOf(languageId), Collections.emptySet()), expectedQuery);
    }

    @ParameterizedTest
    @MethodSource("queryByLanguageName")
    @DisplayName("which contains the name of a programming language")
    void filterByLanguageName(String languageName, String expectedQuery) {
        expectQuery(new FilterProperties(languageName, Collections.emptySet()), expectedQuery);
    }

    @Test
    @DisplayName("which contains the provided tags")
    void filterByLanguage() {
        Set<String> tags = Set.of("Tag-A", "Tag_B", "TagC");
        String[] expectedQuery = new String[]{"+tag:taga", "+tag:tagb", "+tag:tagc"};
        expectQuery(new FilterProperties("", tags), expectedQuery);
    }

    private static Stream<Arguments> queryByLanguageId() {
        return Arrays.stream(Language.values())
                     .map(language -> {
                         String expectedQuery = "+language:" + language.getId();
                         return Arguments.of(language.getId(), expectedQuery);
                     });
    }

    private static Stream<Arguments> queryByLanguageName() {
        return Arrays.stream(Language.values())
                     .map(language -> {
                         String languageName = language.getName().toLowerCase();
                         if (languageName.equals("shell script"))
                             languageName = "shell";
                         else if (languageName.equals("batch script"))
                             languageName = "batch";

                         String expectedQuery = "+language:" + languageName;
                         return Arguments.of(languageName, expectedQuery);
                     });
    }

    private void expectQuery(FilterProperties filterProperties, String... expectedQueryParts) {
        Query query = new FilterQueryBuilder(filterProperties).build();
        assertThat(query.toString()).contains(expectedQueryParts);
    }
}