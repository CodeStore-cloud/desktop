package cloud.codestore.core.usecases.deletesnippet;

import cloud.codestore.core.SnippetNotExistsException;

import javax.annotation.Nonnull;

public interface DeleteSnippetQuery {
    void delete(@Nonnull String snippetId) throws SnippetNotExistsException;
}
