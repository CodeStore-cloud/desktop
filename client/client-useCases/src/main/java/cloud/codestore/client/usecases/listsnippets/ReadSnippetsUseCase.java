package cloud.codestore.client.usecases.listsnippets;

import javax.annotation.Nonnull;

/**
 * Use Case: read a list of code snippets.
 */
public interface ReadSnippetsUseCase {
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
