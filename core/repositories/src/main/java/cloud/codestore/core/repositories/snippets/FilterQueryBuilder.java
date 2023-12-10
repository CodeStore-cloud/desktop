package cloud.codestore.core.repositories.snippets;

import cloud.codestore.core.Language;
import cloud.codestore.core.usecases.listsnippets.FilterProperties;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;

import javax.annotation.Nullable;
import java.util.Collection;

import static cloud.codestore.core.repositories.snippets.SnippetIndex.SnippetField.LANGUAGE;
import static cloud.codestore.core.repositories.snippets.SnippetIndex.SnippetField.TAG;

class FilterQueryBuilder {
    private final FilterProperties filterProperties;
    private final BooleanQuery.Builder filterQuery = new BooleanQuery.Builder();

    FilterQueryBuilder(FilterProperties filterProperties) {
        this.filterProperties = filterProperties;
    }

    Query build() {
        if (filterProperties.isEmpty()) {
            return new MatchAllDocsQuery();
        }

        filterByLanguage(filterProperties.language());
        filterByTags(filterProperties.tags());
        return filterQuery.build();
    }

    private void filterByLanguage(@Nullable Language language) {
        if (language != null) {
            addFilter(LANGUAGE, String.valueOf(language.getId()));
        }
    }

    private void filterByTags(@Nullable Collection<String> tags) {
        if (tags != null) {
            for (String tag : tags) {
                addFilter(TAG, SnippetIndex.normalize(tag));
            }
        }
    }

    private void addFilter(String field, String value) {
        Query query = new TermQuery(new Term(field, value));
        filterQuery.add(query, BooleanClause.Occur.MUST);
    }
}
