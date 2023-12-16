package cloud.codestore.core.usecases.listsnippets;

import cloud.codestore.core.Snippet;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Represents a page which contains a sublist of the list of all available snippets.
 *
 * @param page the number of this page.
 * @param totalPages the number of total pages.
 * @param snippets the list of snippets within this page.
 */
public record SnippetListPage(int page, int totalPages, @Nonnull List<Snippet> snippets) {}
