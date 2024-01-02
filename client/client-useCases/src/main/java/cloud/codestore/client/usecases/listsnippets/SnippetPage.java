package cloud.codestore.client.usecases.listsnippets;

import cloud.codestore.client.Permission;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

/**
 * Represents a page of code snippets.
 *
 * @param snippets    the code snippets within this page.
 * @param nextPageUrl the URL to the next page. May be empty.
 * @param permissions permissions indicating whether new code snippets can be created.
 */
public record SnippetPage(
        @Nonnull List<SnippetListItem> snippets,
        @Nonnull String nextPageUrl,
        @Nonnull Set<Permission> permissions
) {}
