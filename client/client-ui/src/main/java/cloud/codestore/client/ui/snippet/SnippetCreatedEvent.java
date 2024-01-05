package cloud.codestore.client.ui.snippet;

import javax.annotation.Nonnull;

/**
 * An event indicating that a new code snippet was created.
 * @param snippetUri the URI of the new code snippet.
 */
public record SnippetCreatedEvent(@Nonnull String snippetUri) {}
