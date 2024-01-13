package cloud.codestore.client.usecases.listsnippets;

import javax.annotation.Nonnull;

/**
 * Provides information about how to sort the requested code snippets.
 *
 * @param property the snippet property to use for sorting.
 * @param asc whether to sort the code snippets ascending.
 */
public record SortProperties(@Nonnull SnippetProperty property, boolean asc) {
    public enum SnippetProperty {
        RELEVANCE, CREATED, MODIFIED, TITLE
    }

    /**
     * Default sort properties
     */
    public SortProperties() {
        this(SnippetProperty.CREATED, false);
    }

    /**
     * @return whether to sort the code snippets descending.
     */
    public boolean desc() {
        return !asc;
    }
}
