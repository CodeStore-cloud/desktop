package cloud.codestore.client.ui.snippet;

import javax.annotation.Nonnull;

/**
 * An event indicating that a code snippet was deleted.
 * @param snippetUri the URI of the deleted code snippet.
 */
public record SnippetDeletedEvent(@Nonnull String snippetUri) {}
