package cloud.codestore.core.usecases.listsnippets;

import cloud.codestore.core.Snippet;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

/**
 * Represents the result of the repository when searching for code snippets.
 * @param totalCount the total number of snippets found.
 * @param snippetStream a stream providing the corresponding code snippets.
 */
public record SearchResult(int totalCount, @Nonnull Stream<Snippet> snippetStream) {}
