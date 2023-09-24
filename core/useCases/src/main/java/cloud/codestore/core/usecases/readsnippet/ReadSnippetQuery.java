package cloud.codestore.core.usecases.readsnippet;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetNotExistsException;

import javax.annotation.Nonnull;

public interface ReadSnippetQuery {
    Snippet read(@Nonnull String snippetId) throws SnippetNotExistsException;
}
