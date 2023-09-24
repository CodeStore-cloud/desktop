package cloud.codestore.core.usecases.createsnippet;

import cloud.codestore.core.Snippet;

import javax.annotation.Nonnull;

public interface CreateSnippetQuery {
    void create(@Nonnull Snippet snippet);
}
