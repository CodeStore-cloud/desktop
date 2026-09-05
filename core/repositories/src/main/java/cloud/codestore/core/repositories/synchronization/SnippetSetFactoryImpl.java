package cloud.codestore.core.repositories.synchronization;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.repositories.Directory;
import cloud.codestore.core.repositories.serialization.SnippetReader;
import cloud.codestore.core.repositories.serialization.SnippetWriter;
import cloud.codestore.core.repositories.synchronization.service.GoogleDriveService;
import cloud.codestore.core.usecases.synchronizesnippets.CloudService;
import cloud.codestore.core.usecases.synchronizesnippets.SnippetSetFactory;
import cloud.codestore.synchronization.ItemSet;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
class SnippetSetFactoryImpl implements SnippetSetFactory {

    private final Directory snippetsDirectory;
    private final SnippetReader snippetReader;
    private final SnippetWriter snippetWriter;
    private final GoogleDriveService googleDriveService;

    SnippetSetFactoryImpl(
            @Qualifier("snippets") Directory snippetsDirectory,
            SnippetReader snippetReader,
            SnippetWriter snippetWriter,
            GoogleDriveService googleDriveService
    ) {
        this.snippetsDirectory = snippetsDirectory;
        this.snippetReader = snippetReader;
        this.snippetWriter = snippetWriter;
        this.googleDriveService = googleDriveService;
    }

    @Nonnull
    @Override
    public ItemSet<Snippet> createLocalSnippetSet() {
        return new LocalSnippetSet(snippetsDirectory, snippetReader, snippetWriter);
    }

    @Nonnull
    @Override
    public ItemSet<Snippet> createRemoteSnippetSet(@Nonnull CloudService cloudService) {
        RemoteDirectory remoteCodeStoreDirectory = switch (cloudService) {
            case GOOGLE_DRIVE -> googleDriveService.login();
            default -> throw new IllegalArgumentException("Invalid cloud service " + cloudService);
        };
        return new RemoteSnippetSet(snippetReader, snippetWriter, remoteCodeStoreDirectory);
    }
}
