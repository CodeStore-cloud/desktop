package cloud.codestore.core.repositories.synchronization;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.repositories.Directory;
import cloud.codestore.core.usecases.createsnippet.CreateSnippetQuery;
import cloud.codestore.core.usecases.deletesnippet.DeleteSnippetQuery;
import cloud.codestore.core.usecases.readsnippet.ReadSnippetQuery;
import cloud.codestore.core.usecases.synchronizesnippets.SnippetSetFactory;
import cloud.codestore.core.usecases.updatesnippet.UpdateSnippetQuery;
import cloud.codestore.synchronization.ItemSet;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
class SnippetSetFactoryImpl implements SnippetSetFactory {

    private final Directory snippetsDirectory;
    private final Directory driveTokensDirectory;
    private final ReadSnippetQuery readSnippetQuery;
    private final CreateSnippetQuery createSnippetQuery;
    private final DeleteSnippetQuery deleteSnippetQuery;
    private final UpdateSnippetQuery updateSnippetQuery;

    SnippetSetFactoryImpl(
            @Qualifier("snippets") Directory snippetsDirectory,
            @Qualifier("googleDriveTokens") Directory driveTokensDirectory,
            ReadSnippetQuery readSnippetQuery,
            CreateSnippetQuery createSnippetQuery,
            DeleteSnippetQuery deleteSnippetQuery,
            UpdateSnippetQuery updateSnippetQuery
    ) {
        this.snippetsDirectory = snippetsDirectory;
        this.driveTokensDirectory = driveTokensDirectory;
        this.readSnippetQuery = readSnippetQuery;
        this.createSnippetQuery = createSnippetQuery;
        this.deleteSnippetQuery = deleteSnippetQuery;
        this.updateSnippetQuery = updateSnippetQuery;
    }

    @Override
    public ItemSet<Snippet> createLocalSnippetSet() {
        return new LocalSnippetSet(
                snippetsDirectory,
                readSnippetQuery,
                createSnippetQuery,
                deleteSnippetQuery,
                updateSnippetQuery
        );
    }

    @Override
    public ItemSet<Snippet> createRemoteSnippetSet() {
        new GoogleDriveRemoteSnippetSet(driveTokensDirectory);

        return null;
    }
}
