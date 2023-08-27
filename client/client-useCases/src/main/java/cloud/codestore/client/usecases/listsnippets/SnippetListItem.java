package cloud.codestore.client.usecases.listsnippets;

import javax.annotation.Nonnull;

/**
 * Represents a code snippet as part of the selection list.
 */
public record SnippetListItem(@Nonnull String uri, @Nonnull String title) {}