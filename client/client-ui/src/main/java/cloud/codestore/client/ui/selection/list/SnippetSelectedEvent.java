package cloud.codestore.client.ui.selection.list;

/**
 * An event which is fired when a new code snippet is selected.
 *
 * @param snippetUri the URI of the selected code snippet.
 */
public record SnippetSelectedEvent(String snippetUri) {}
