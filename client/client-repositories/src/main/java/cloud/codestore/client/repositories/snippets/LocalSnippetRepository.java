package cloud.codestore.client.repositories.snippets;

import cloud.codestore.client.Snippet;
import cloud.codestore.client.SnippetBuilder;
import cloud.codestore.client.SnippetRepository;
import cloud.codestore.client.repositories.HttpClient;
import cloud.codestore.jsonapi.document.ResourceCollectionDocument;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * A {@link SnippetRepository} which saves/loads the code snippets from the local {CodeStore} Core.
 */
class LocalSnippetRepository implements SnippetRepository {

    private final HttpClient client;

    LocalSnippetRepository(HttpClient client) {
        this.client = client;
    }

    @Nonnull
    @Override
    public List<Snippet> get() {
        String snippetCollectionUrl = client.getSnippetCollectionUrl();
        ResourceCollectionDocument<SnippetResource> resourceCollection = client.getCollection(snippetCollectionUrl, SnippetResource.class);
        return Arrays.stream(resourceCollection.getData())
                     .map(snippetResource -> new SnippetBuilder().uri(snippetResource.getSelfLink())
                                                                 .title(snippetResource.getTitle())
                                                                 .build()
                     )
                     .toList();
    }
}
