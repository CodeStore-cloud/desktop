package cloud.codestore.core.repositories.snippets;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetNotExistsException;
import cloud.codestore.core.repositories.Directory;
import cloud.codestore.core.repositories.File;
import cloud.codestore.core.repositories.Repository;
import cloud.codestore.core.usecases.createsnippet.CreateSnippetQuery;
import cloud.codestore.core.usecases.deletesnippet.DeleteSnippetQuery;
import cloud.codestore.core.usecases.listsnippets.FilterProperties;
import cloud.codestore.core.usecases.listsnippets.ReadSnippetsQuery;
import cloud.codestore.core.usecases.readsnippet.ReadSnippetQuery;
import cloud.codestore.core.usecases.updatesnippet.UpdateSnippetQuery;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Represents a repository which loads/saves the code snippets on the file system.
 */
@Repository
class LocalSnippetRepository implements CreateSnippetQuery, UpdateSnippetQuery, DeleteSnippetQuery, ReadSnippetQuery, ReadSnippetsQuery {
    static final String JSON_FILE_EXTENSION = ".json";

    private final Directory snippetsDirectory;
    private final SnippetReader snippetReader;
    private final SnippetWriter snippetWriter;

    LocalSnippetRepository(
            @Qualifier("snippets") Directory snippetsDirectory,
            SnippetReader snippetReader,
            SnippetWriter snippetWriter
    ) {
        this.snippetsDirectory = snippetsDirectory;
        this.snippetReader = snippetReader;
        this.snippetWriter = snippetWriter;
    }

    @Override
    public void create(@Nonnull Snippet snippet) {
        File file = getSnippetFile(snippet.getId());
        snippetWriter.write(snippet, file);
    }

    @Override
    public List<Snippet> readSnippets(@Nonnull FilterProperties filterProperties) {
        return snippetsDirectory.getFiles()
                                .stream()
                                .map(snippetReader::read)
                                .toList();
    }

    @Override
    public Snippet read(@Nonnull String snippetId) throws SnippetNotExistsException {
        File file = requireExists(getSnippetFile(snippetId));
        return snippetReader.read(file);
    }

    @Override
    public void update(@Nonnull Snippet snippet) throws SnippetNotExistsException {
        File file = requireExists(getSnippetFile(snippet.getId()));
        snippetWriter.write(snippet, file);
    }

    @Override
    public void delete(@Nonnull String snippetId) throws SnippetNotExistsException {
        requireExists(getSnippetFile(snippetId)).delete();
    }

    private File getSnippetFile(String snippetId) {
        return snippetsDirectory.getFile(snippetId + JSON_FILE_EXTENSION);
    }

    private File requireExists(File file) throws SnippetNotExistsException {
        if (!file.exists()) {
            throw new SnippetNotExistsException();
        }

        return file;
    }
}
