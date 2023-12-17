package cloud.codestore.core.usecases.listsnippets;

import javax.annotation.Nonnull;

public interface ReadSnippetsQuery {
    SearchResult readSnippets(
            @Nonnull String search,
            @Nonnull FilterProperties filterProperties,
            @Nonnull SortProperties sortProperties
    );
}
