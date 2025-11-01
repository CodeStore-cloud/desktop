package cloud.codestore.core.repositories.index;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetNotExistsException;
import cloud.codestore.core.repositories.Directory;
import cloud.codestore.core.repositories.File;
import cloud.codestore.core.repositories.serialization.SnippetFileHelper;
import cloud.codestore.core.repositories.serialization.SnippetReader;
import cloud.codestore.core.repositories.serialization.SnippetWriter;
import cloud.codestore.core.usecases.createsnippet.CreateSnippetQuery;
import cloud.codestore.core.usecases.deletesnippet.DeleteSnippetQuery;
import cloud.codestore.core.usecases.readsnippet.ReadSnippetQuery;
import cloud.codestore.core.usecases.updatesnippet.UpdateSnippetQuery;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

/**
 * Represents a repository which loads/saves the code snippets on the file system.
 */
@Component
class FileSystemRepository implements CreateSnippetQuery, UpdateSnippetQuery, DeleteSnippetQuery, ReadSnippetQuery {
    private final Directory snippetsDirectory;
    private final SnippetReader snippetReader;
    private final SnippetWriter snippetWriter;

    FileSystemRepository(
            @Qualifier("snippets") Directory snippetsDirectory,
            SnippetReader snippetReader,
            SnippetWriter snippetWriter
    ) {
        this.snippetsDirectory = snippetsDirectory;
        this.snippetReader = snippetReader;
        this.snippetWriter = snippetWriter;
    }

    /**
     * @return all available code snippets as stream.
     */
    Stream<Snippet> readSnippets() {
        return snippetsDirectory.getFiles()
                                .stream()
                                .map(snippetReader::read);
    }

    /**
     * Loads all snippets defined by the given IDs.
     * @param snippetIds a stream of snippet IDs.
     * @return the corresponding code snippets.
     */
    Stream<Snippet> readSnippets(@Nonnull Stream<String> snippetIds) {
        return snippetIds.map(this::file)
                         .map(snippetReader::read);
    }

    @Override
    public void create(@Nonnull Snippet snippet) {
        File file = file(snippet.getId());
        snippetWriter.write(snippet, file);
    }

    @Override
    public Snippet read(@Nonnull String snippetId) throws SnippetNotExistsException {
        File file = existingFile(snippetId);
        return snippetReader.read(file);
    }

    @Override
    public void update(@Nonnull Snippet snippet) throws SnippetNotExistsException {
        File file = existingFile(snippet.getId());
        snippetWriter.write(snippet, file);
    }

    @Override
    public void delete(@Nonnull String snippetId) throws SnippetNotExistsException {
        existingFile(snippetId).delete();
    }

    private File existingFile(String snippetId) throws SnippetNotExistsException {
        File file = file(snippetId);
        if (!file.exists()) {
            throw new SnippetNotExistsException();
        }

        return file;
    }

    private File file(String snippetId) {
        return snippetsDirectory.getFile(SnippetFileHelper.getFileName(snippetId));
    }
}
