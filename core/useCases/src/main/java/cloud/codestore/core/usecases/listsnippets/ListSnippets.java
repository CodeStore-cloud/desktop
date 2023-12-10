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
    private final ReadSnippetsQuery query;

    public ListSnippets(ReadSnippetsQuery query) {
        this.query = query;
    }

    @Nonnull
    public List<Snippet> list(
            @Nonnull String search,
            @Nonnull FilterProperties filterProperties,
            @Nullable SortProperties sortProperties
    ) {
        sortProperties = Optional.ofNullable(sortProperties)
                                 .orElseGet(() -> search.isEmpty() ? new SortProperties() : new SortProperties(RELEVANCE, true));

        return query.readSnippets(search, filterProperties, sortProperties);
    }
}
