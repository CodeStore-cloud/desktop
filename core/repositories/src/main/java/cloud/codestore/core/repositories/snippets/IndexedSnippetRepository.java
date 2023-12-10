package cloud.codestore.core.repositories.snippets;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetNotExistsException;
import cloud.codestore.core.repositories.Repository;
import cloud.codestore.core.repositories.tags.TagRepository;
import cloud.codestore.core.usecases.createsnippet.CreateSnippetQuery;
import cloud.codestore.core.usecases.deletesnippet.DeleteSnippetQuery;
import cloud.codestore.core.usecases.listsnippets.FilterProperties;
import cloud.codestore.core.usecases.listsnippets.ReadSnippetsQuery;
import cloud.codestore.core.usecases.listsnippets.SortProperties;
import cloud.codestore.core.usecases.readsnippet.ReadSnippetQuery;
import cloud.codestore.core.usecases.updatesnippet.UpdateSnippetQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Stream;

import static cloud.codestore.core.repositories.snippets.SnippetIndex.SnippetField;

/**
 * A decorator around the {@link FileSystemRepository} which stores code snippets in the {@link SnippetIndex}.
 */
@Primary
@Repository
class IndexedSnippetRepository implements CreateSnippetQuery, UpdateSnippetQuery, DeleteSnippetQuery, ReadSnippetQuery, ReadSnippetsQuery {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexedSnippetRepository.class);

    private SnippetIndex index;
    private FileSystemRepository fsRepo;
    private TagRepository tagRepository;

    IndexedSnippetRepository(SnippetIndex index, FileSystemRepository fsRepo, TagRepository tagRepository) {
        this.index = index;
        this.fsRepo = fsRepo;
        this.tagRepository = tagRepository;
        indexAllSnippets();
    }

    @Override
    public void create(@Nonnull Snippet snippet) {
        fsRepo.create(snippet);
        index.add(snippet);
    }

    @Override
    public void update(@Nonnull Snippet snippet) throws SnippetNotExistsException {
        fsRepo.update(snippet);
        index.update(snippet);
    }

    @Override
    public void delete(@Nonnull String snippetId) throws SnippetNotExistsException {
        fsRepo.delete(snippetId);
        index.remove(snippetId);
    }

    @Override
    public List<Snippet> readSnippets(
            @Nonnull String search,
            @Nonnull FilterProperties filterProperties,
            @Nonnull SortProperties sortProperties
    ) {
        Query filterQuery = new FilterQueryBuilder(filterProperties).buildFilterQuery();
        SortField sortField = toSortFields(sortProperties);
        Stream<String> snippetIds = index.query(filterQuery, sortField);
        return fsRepo.readSnippets(snippetIds).toList();
    }

    @Override
    public Snippet read(@Nonnull String snippetId) throws SnippetNotExistsException {
        return fsRepo.read(snippetId);
    }

    private void indexAllSnippets() {
        long startTime = System.currentTimeMillis();

        Stream<Snippet> snippetStream = fsRepo.readSnippets();
        snippetStream = snippetStream.peek(snippet -> tagRepository.add(snippet.getTags()));
        index.add(snippetStream);

        long endTime = System.currentTimeMillis();
        LOGGER.info("Indexing finished after " + (endTime - startTime) + "ms");
    }

    private SortField toSortFields(SortProperties sortProperties) {
        return switch(sortProperties.property())
        {
            case RELEVANCE -> null;
            case TITLE -> new SortField(SnippetField.TITLE, SortField.Type.STRING, sortProperties.desc());
            case CREATED -> new SortField(SnippetField.CREATED, SortField.Type.LONG, sortProperties.desc());
            case MODIFIED -> new SortField(SnippetField.MODIFIED, SortField.Type.LONG, sortProperties.desc());
        };
    }
}
