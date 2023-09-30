package cloud.codestore.core.repositories.snippets;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetNotExistsException;
import cloud.codestore.core.repositories.Repository;
import cloud.codestore.core.usecases.createsnippet.CreateSnippetQuery;
import cloud.codestore.core.usecases.deletesnippet.DeleteSnippetQuery;
import cloud.codestore.core.usecases.listsnippets.FilterProperties;
import cloud.codestore.core.usecases.listsnippets.ReadSnippetsQuery;
import cloud.codestore.core.usecases.readsnippet.ReadSnippetQuery;
import cloud.codestore.core.usecases.updatesnippet.UpdateSnippetQuery;
import org.springframework.context.annotation.Primary;

import javax.annotation.Nonnull;
import java.util.List;

@Primary
@Repository
public class IndexedSnippetRepository implements CreateSnippetQuery, UpdateSnippetQuery, DeleteSnippetQuery, ReadSnippetQuery, ReadSnippetsQuery {
    private SnippetIndex index;
    private FileSystemRepository localRepo;

    public IndexedSnippetRepository(SnippetIndex index, FileSystemRepository localRepo) {
        this.index = index;
        this.localRepo = localRepo;
    }

    @Override
    public void create(@Nonnull Snippet snippet) {
        localRepo.create(snippet);
        index.add(snippet);
    }

    @Override
    public void update(@Nonnull Snippet snippet) throws SnippetNotExistsException {
        localRepo.update(snippet);
        index.update(snippet);
    }

    @Override
    public void delete(@Nonnull String snippetId) throws SnippetNotExistsException {
        localRepo.delete(snippetId);
        index.remove(snippetId);
    }

    @Override
    public List<Snippet> readSnippets(@Nonnull FilterProperties filterProperties) {
        return localRepo.readSnippets(filterProperties);
    }

    @Override
    public Snippet read(@Nonnull String snippetId) throws SnippetNotExistsException {
        return localRepo.read(snippetId);
    }
}
