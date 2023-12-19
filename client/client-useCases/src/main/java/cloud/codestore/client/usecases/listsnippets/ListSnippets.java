package cloud.codestore.client.usecases.listsnippets;

import cloud.codestore.client.UseCase;

import javax.annotation.Nonnull;

/**
 * Use case: read a collection of code snippets.
 */
@UseCase
public class ListSnippets {
    private final ReadSnippetPageQuery pageQuery;

    public ListSnippets(ReadSnippetPageQuery pageQuery) {
        this.pageQuery = pageQuery;
    }

    /**
     * Reads the first page of code snippets.
     *
     * @return a page containing a list of code snippets.
     */
    @Nonnull
    public SnippetPage readSnippets() {
        return pageQuery.getFirstPage();
    }

    /**
     * Reads a specific page of code snippets.
     *
     * @param pageUrl the URL of the page to read.
     * @return a page containing a list of code snippets.
     */
    @Nonnull
    public SnippetPage readSnippets(@Nonnull String pageUrl) {
        return pageQuery.getPage(pageUrl);
    }
}
