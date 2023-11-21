package cloud.codestore.client.repositories.snippets;

import cloud.codestore.client.Snippet;
import cloud.codestore.client.repositories.HttpClient;
import cloud.codestore.client.repositories.Repository;
import cloud.codestore.client.repositories.tags.TagRepository;
import cloud.codestore.client.usecases.listsnippets.SnippetListItem;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * A {@link cloud.codestore.client.SnippetRepository} which saves/loads the code snippets from the local {CodeStore} Core.
 */
@Repository
class SnippetRepository implements cloud.codestore.client.SnippetRepository {

    private final HttpClient client;
    private final TagRepository tagRepository;

    SnippetRepository(HttpClient client, TagRepository tagRepository) {
        this.client = client;
        this.tagRepository = tagRepository;
    }

    @Nonnull
    @Override
    public List<SnippetListItem> get() {
        var resourceCollection = client.getCollection(client.getSnippetCollectionUrl(), SnippetResource.class);
        return Arrays.stream(resourceCollection.getData())
                     .map(resource -> new SnippetListItem(resource.getSelfLink(), resource.getTitle()))
                     .toList();
    }

    @Nonnull
    @Override
    public Snippet get(String snippetUri) {
        SnippetResource snippetResource = client.get(snippetUri, SnippetResource.class).getData();
        String tagsUri = snippetResource.getTags().getRelatedResourceLink();

        return Snippet.builder()
                      .uri(snippetResource.getSelfLink())
                      .title(snippetResource.getTitle())
                      .description(snippetResource.getDescription())
                      .code(snippetResource.getCode())
                      .tags(tagRepository.get(tagsUri))
                      .build();
    }
}
