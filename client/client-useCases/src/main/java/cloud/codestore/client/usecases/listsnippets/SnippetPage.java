package cloud.codestore.client.usecases.listsnippets;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Represents a page of code snippets.
 *
 * @param snippets    the code snippets within this page.
 * @param nextPageUrl the URL to the next page. May be empty.
 */
public record SnippetPage(@Nonnull List<SnippetListItem> snippets, @Nonnull String nextPageUrl) {}
