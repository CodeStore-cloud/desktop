package cloud.codestore.client.usecases.listsnippets;

import javax.annotation.Nonnull;

/**
 * A query for reading a list of code snippets.
 */
public interface ReadSnippetPageQuery {
    /**
     * Reads the first page of code snippets.
     * @return a page containing a list of code snippets.
     */
    @Nonnull
    SnippetPage getFirstPage();

    /**
     * Reads a page of code snippets.
     * @param pageUrl the URL of a specific page.
     * @return a page containing a list of code snippets.
     */
    @Nonnull
    SnippetPage getPage(@Nonnull String pageUrl);
}
