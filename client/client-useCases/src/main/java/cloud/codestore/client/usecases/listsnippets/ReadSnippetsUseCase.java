package cloud.codestore.client.usecases.listsnippets;

import javax.annotation.Nonnull;

/**
 * Use Case: read a list of code snippets.
 */
public interface ReadSnippetsUseCase {
    /**
     * Reads the first page of code snippets.
     *
     * @param searchQuery      the full-text-search input. May be empty.
     * @param filterProperties defines how the snippets should be filtered.
     * @param sortProperties   defines how the snippets should be sorted.
     * @return a page containing a list of code snippets.
     */
    @Nonnull
    SnippetPage getPage(
            @Nonnull String searchQuery,
            @Nonnull FilterProperties filterProperties,
            @Nonnull SortProperties sortProperties
    );

    /**
     * Reads a page of code snippets.
     *
     * @param pageUrl the URL of a specific page.
     * @return a page containing a list of code snippets.
     */
    @Nonnull
    SnippetPage getPage(@Nonnull String pageUrl);
}
