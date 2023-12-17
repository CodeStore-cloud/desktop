package cloud.codestore.core.usecases.listsnippets;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.UseCase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static cloud.codestore.core.usecases.listsnippets.SortProperties.SnippetProperty.RELEVANCE;

/**
 * Use case: read all code snippets.
 */
@UseCase
public class ListSnippets {
    private static final int PAGE_SIZE = 50;

    private final ReadSnippetsQuery readSnippetsQuery;

    public ListSnippets(ReadSnippetsQuery readSnippetsQuery) {
        this.readSnippetsQuery = readSnippetsQuery;
    }

    @Nonnull
    public SnippetListPage list(
            @Nonnull String search,
            @Nonnull FilterProperties filterProperties,
            @Nullable SortProperties sortProperties,
            int pageNumber
    ) throws PageNotExistsException {
        sortProperties = Optional.ofNullable(sortProperties)
                                 .orElseGet(() -> search.isEmpty() ? new SortProperties() : new SortProperties(RELEVANCE, true));

        var searchResult = readSnippetsQuery.readSnippets(search, filterProperties, sortProperties);

        int totalPages = (int) Math.max(1, Math.ceil(searchResult.totalCount() / (double) PAGE_SIZE));
        if (pageNumber <= 0 || pageNumber > totalPages)
            throw new PageNotExistsException(pageNumber);

        List<Snippet> snippets = searchResult.snippetStream()
                                             .skip((long) (pageNumber - 1) * PAGE_SIZE)
                                             .limit(PAGE_SIZE)
                                             .toList();

        return new SnippetListPage(pageNumber, totalPages, snippets);
    }
}
