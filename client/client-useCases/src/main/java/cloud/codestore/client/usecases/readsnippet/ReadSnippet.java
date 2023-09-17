package cloud.codestore.client.usecases.readsnippet;

import cloud.codestore.client.Snippet;
import cloud.codestore.client.SnippetRepository;
import cloud.codestore.client.UseCase;

import javax.annotation.Nonnull;

@UseCase
public class ReadSnippet {
    private final SnippetRepository repository;

    public ReadSnippet(SnippetRepository repository) {
        this.repository = repository;
    }

    public Snippet readSnippet(@Nonnull String snippetUri) {
        return repository.get(snippetUri);
    }
}
