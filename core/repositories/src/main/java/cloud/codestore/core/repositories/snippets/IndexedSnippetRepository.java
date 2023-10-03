package cloud.codestore.core.repositories.snippets;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetNotExistsException;
import cloud.codestore.core.repositories.Repository;
import cloud.codestore.core.repositories.tags.TagRepository;
import cloud.codestore.core.usecases.createsnippet.CreateSnippetQuery;
import cloud.codestore.core.usecases.deletesnippet.DeleteSnippetQuery;
import cloud.codestore.core.usecases.listsnippets.FilterProperties;
import cloud.codestore.core.usecases.listsnippets.ReadSnippetsQuery;
import cloud.codestore.core.usecases.readsnippet.ReadSnippetQuery;
import cloud.codestore.core.usecases.updatesnippet.UpdateSnippetQuery;
import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Stream;

/**
 * A decorator around the {@link FileSystemRepository} which stores code snippets in the {@link SnippetIndex}.
 */
@Primary
@Repository
public class IndexedSnippetRepository implements CreateSnippetQuery, UpdateSnippetQuery, DeleteSnippetQuery, ReadSnippetQuery, ReadSnippetsQuery {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexedSnippetRepository.class);

    private SnippetIndex index;
    private FileSystemRepository localRepo;
    private TagRepository tagRepository;

    public IndexedSnippetRepository(SnippetIndex index, FileSystemRepository localRepo, TagRepository tagRepository) {
        this.index = index;
        this.localRepo = localRepo;
        this.tagRepository = tagRepository;
        indexAllSnippets();
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
        Query filterQuery = new FilterQueryBuilder(filterProperties).buildFilterQuery();
        Stream<String> snippetIds = index.query(filterQuery);
        return localRepo.readSnippets(snippetIds).toList();
    }

    @Override
    public Snippet read(@Nonnull String snippetId) throws SnippetNotExistsException {
        return localRepo.read(snippetId);
    }

    private void indexAllSnippets() {
        long startTime = System.currentTimeMillis();

        Stream<Snippet> snippetStream = localRepo.readSnippets();
        snippetStream = snippetStream.peek(snippet -> tagRepository.add(snippet.getTags()));
        index.add(snippetStream);

        long endTime = System.currentTimeMillis();
        LOGGER.info("Indexing finished after " + (endTime - startTime) + "ms");
    }
}
