package cloud.codestore.client.ui.selection.list;

import javax.annotation.Nonnull;

/**
 * An event which is fired when a new code snippet is selected and shown in the UI.
 * This event is a confirmation of the {@link RequestSnippetSelectionEvent}.
 *
 * @param snippetUri the URI of the selected code snippet. Must not be {@code null}.
 */
public record SnippetSelectedEvent(@Nonnull String snippetUri) {}
