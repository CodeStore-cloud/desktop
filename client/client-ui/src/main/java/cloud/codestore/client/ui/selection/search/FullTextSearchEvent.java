package cloud.codestore.client.ui.selection.search;

/**
 * An event which is fired when the full-text-search input has changed.
 *
 * @param searchQuery the full-text-search input.
 */
public record FullTextSearchEvent(String searchQuery) {}
