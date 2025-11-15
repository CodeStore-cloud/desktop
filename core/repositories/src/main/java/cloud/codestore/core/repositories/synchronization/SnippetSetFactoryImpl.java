package cloud.codestore.core.repositories.synchronization;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.repositories.Directory;
import cloud.codestore.core.repositories.serialization.SnippetReader;
import cloud.codestore.core.repositories.serialization.SnippetWriter;
import cloud.codestore.core.repositories.synchronization.service.GoogleDriveAuthenticator;
import cloud.codestore.core.usecases.synchronizesnippets.CloudService;
import cloud.codestore.core.usecases.synchronizesnippets.SnippetSetFactory;
import cloud.codestore.synchronization.ItemSet;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
class SnippetSetFactoryImpl implements SnippetSetFactory {

    private final Directory snippetsDirectory;
    private final GoogleDriveAuthenticator googleDriveAuthenticator;
    private final SnippetReader snippetReader;
    private final SnippetWriter snippetWriter;

    SnippetSetFactoryImpl(
            @Qualifier("snippets") Directory snippetsDirectory,
            GoogleDriveAuthenticator googleDriveAuthenticator,
            SnippetReader snippetReader,
            SnippetWriter snippetWriter
    ) {
        this.snippetsDirectory = snippetsDirectory;
        this.googleDriveAuthenticator = googleDriveAuthenticator;
        this.snippetReader = snippetReader;
        this.snippetWriter = snippetWriter;
    }

    @Override
    public ItemSet<Snippet> createLocalSnippetSet() {
        return new LocalSnippetSet(snippetsDirectory, snippetReader, snippetWriter);
    }

    @Override
    public ItemSet<Snippet> createRemoteSnippetSet(CloudService cloudService) {
        return switch (cloudService) {
            case GOOGLE_DRIVE -> new GoogleDriveSnippetSet(googleDriveAuthenticator, snippetReader, snippetWriter);
            default -> throw new IllegalStateException("No cloud service configured.");
        };
    }
}
