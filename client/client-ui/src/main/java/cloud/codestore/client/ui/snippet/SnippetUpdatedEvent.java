package cloud.codestore.client.ui.snippet;

import javax.annotation.Nonnull;

/**
 * An event indicating that a code snippet was updated.
 * @param snippetUri the URI of the code snippet.
 */
public record SnippetUpdatedEvent(@Nonnull String snippetUri) {}
