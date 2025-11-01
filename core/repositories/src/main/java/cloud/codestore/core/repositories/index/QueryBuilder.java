package cloud.codestore.core.repositories.index;

import cloud.codestore.core.usecases.listsnippets.FilterProperties;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

class QueryBuilder {
    private SearchQueryBuilder searchQueryBuilder;
    private FilterQueryBuilder filterQueryBuilder;

    QueryBuilder(String search, FilterProperties filterProperties) {
        searchQueryBuilder = new SearchQueryBuilder(search);
        filterQueryBuilder = new FilterQueryBuilder(filterProperties);
    }

    Query build() {
        return new BooleanQuery.Builder()
                .add(searchQueryBuilder.build(), BooleanClause.Occur.MUST)
                .add(filterQueryBuilder.build(), BooleanClause.Occur.MUST)
                .build();
    }
}
