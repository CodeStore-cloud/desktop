package cloud.codestore.core.usecases.listsnippets;

import cloud.codestore.core.UseCase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

import static cloud.codestore.core.usecases.listsnippets.SortProperties.SnippetProperty.RELEVANCE;

/**
 * Use case: read all code snippets.
 */
@UseCase
public class ListSnippets {
    private static final int PAGE_SIZE = 50;

    private final ReadSnippetsQuery readSnippetsQuery;
    private final CountSnippetsQuery countSnippetsQuery;

    public ListSnippets(ReadSnippetsQuery readSnippetsQuery, CountSnippetsQuery countSnippetsQuery) {
        this.readSnippetsQuery = readSnippetsQuery;
        this.countSnippetsQuery = countSnippetsQuery;
    }

    @Nonnull
    public SnippetListPage list(
            @Nonnull String search,
            @Nonnull FilterProperties filterProperties,
            @Nullable SortProperties sortProperties,
            int pageNumber
    ) throws PageNotExistsException {
        int snippetCount = countSnippetsQuery.getSnippetCount();
        int totalPages = (int) Math.max(1, Math.ceil(snippetCount / (double) PAGE_SIZE));

        if (pageNumber <= 0 || pageNumber > totalPages)
            throw new PageNotExistsException(pageNumber);

        sortProperties = Optional.ofNullable(sortProperties)
                                 .orElseGet(() -> search.isEmpty() ? new SortProperties() : new SortProperties(RELEVANCE, true));

        var snippets = readSnippetsQuery.readSnippets(search, filterProperties, sortProperties);
        return new SnippetListPage(pageNumber, totalPages, snippets);
    }
}
