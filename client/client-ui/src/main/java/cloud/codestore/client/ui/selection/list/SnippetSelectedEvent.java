package cloud.codestore.client.ui.selection.list;

import javax.annotation.Nonnull;

/**
 * An event which is fired when a new code snippet is selected.
 *
 * @param snippetUri the URI of the selected code snippet. Must not be {@code null}.
 */
public record SnippetSelectedEvent(@Nonnull String snippetUri) {}
