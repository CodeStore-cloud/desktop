package cloud.codestore.core.usecases.updatesnippet;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetNotExistsException;

import javax.annotation.Nonnull;

public interface UpdateSnippetQuery {
    void update(@Nonnull Snippet snippet) throws SnippetNotExistsException;
}
