package cloud.codestore.core.repositories.snippets;

import cloud.codestore.core.Language;
import cloud.codestore.core.usecases.listsnippets.FilterProperties;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;

import javax.annotation.Nullable;

import static cloud.codestore.core.repositories.snippets.SnippetIndex.SnippetField.LANGUAGE;

class FilterQueryBuilder {
    private final FilterProperties filterProperties;
    private final BooleanQuery.Builder filterQuery = new BooleanQuery.Builder();

    FilterQueryBuilder(FilterProperties filterProperties) {
        this.filterProperties = filterProperties;
    }

    Query buildFilterQuery() {
        if (filterProperties.isEmpty()) {
            return new MatchAllDocsQuery();
        }

        filterByLanguage(filterProperties.language());
        return filterQuery.build();
    }

    private void filterByLanguage(@Nullable Language language) {
        if (language != null) {
            addFilter(LANGUAGE, String.valueOf(language.getId()));
        }
    }

    private void addFilter(String field, String value) {
        Query query = new TermQuery(new Term(field, value));
        filterQuery.add(query, BooleanClause.Occur.MUST);
    }
}
