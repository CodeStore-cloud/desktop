package cloud.codestore.client.repositories.snippets;

import cloud.codestore.client.Snippet;
import cloud.codestore.client.repositories.HttpClient;
import cloud.codestore.client.repositories.Repository;
import cloud.codestore.client.repositories.tags.LocalTagRepository;
import cloud.codestore.client.usecases.listsnippets.ReadSnippetPageQuery;
import cloud.codestore.client.usecases.listsnippets.SnippetListItem;
import cloud.codestore.client.usecases.listsnippets.SnippetPage;
import cloud.codestore.client.usecases.readsnippet.ReadSnippetQuery;
import cloud.codestore.jsonapi.link.Link;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Optional;

/**
 * A repository which saves/loads code snippets from the local {CodeStore} Core.
 */
@Repository
class LocalSnippetRepository implements ReadSnippetPageQuery, ReadSnippetQuery {

    private final HttpClient client;
    private final LocalTagRepository tagRepository;

    LocalSnippetRepository(HttpClient client, LocalTagRepository tagRepository) {
        this.client = client;
        this.tagRepository = tagRepository;
    }

    @Nonnull
    @Override
    public SnippetPage getFirstPage() {
        return getPage(client.getSnippetCollectionUrl());
    }

    @Nonnull
    @Override
    public SnippetPage getPage(@Nonnull String pageUrl) {
        var resourceCollection = client.getCollection(pageUrl, SnippetResource.class);
        var items = Arrays.stream(resourceCollection.getData(SnippetResource.class))
                          .map(resource -> new SnippetListItem(resource.getSelfLink(), resource.getTitle()))
                          .toList();

        String nextPageLink = resourceCollection.getLinks().getHref(Link.NEXT);
        String nextPageUrl = Optional.ofNullable(nextPageLink).orElse("");
        return new SnippetPage(items, nextPageUrl);
    }

    @Nonnull
    @Override
    public Snippet readSnippet(String snippetUri) {
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
