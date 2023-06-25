package cloud.codestore.core.repositories.snippets;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetRepository;
import cloud.codestore.core.repositories.Directory;
import cloud.codestore.core.repositories.File;
import cloud.codestore.core.repositories.Repository;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

/**
 * A {@link SnippetRepository} which loads/saves the code snippets on the file system.
 */
@Repository
class LocalSnippetRepository implements SnippetRepository {
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
    public boolean contains(String snippetId) {
        File file = getSnippetFile(snippetId);
        return file.exists();
    }

    @Override
    public Snippet get(String snippetId) {
        File file = getSnippetFile(snippetId);
        return snippetReader.read(file);
    }

    @Override
    public List<Snippet> get() {
        return snippetsDirectory.getFiles()
                                .stream()
                                .map(snippetReader::read)
                                .toList();
    }

    @Override
    public void put(Snippet snippet) {
        File file = getSnippetFile(snippet.getId());
        snippetWriter.write(snippet, file);
    }

    @Override
    public void delete(String snippetId) {
        getSnippetFile(snippetId).delete();
    }

    private File getSnippetFile(String snippetId) {
        return snippetsDirectory.getFile(snippetId + JSON_FILE_EXTENSION);
    }
}
