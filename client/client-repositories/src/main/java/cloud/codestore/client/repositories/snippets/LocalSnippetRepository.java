package cloud.codestore.client.repositories.snippets;

import cloud.codestore.client.Permission;
import cloud.codestore.client.Snippet;
import cloud.codestore.client.repositories.HttpClient;
import cloud.codestore.client.repositories.Repository;
import cloud.codestore.client.repositories.ResourceMetaInfo;
import cloud.codestore.client.repositories.tags.LocalTagRepository;
import cloud.codestore.client.usecases.deletesnippet.DeleteSnippetUseCase;
import cloud.codestore.client.usecases.listsnippets.FilterProperties;
import cloud.codestore.client.usecases.listsnippets.ReadSnippetsUseCase;
import cloud.codestore.client.usecases.listsnippets.SnippetListItem;
import cloud.codestore.client.usecases.listsnippets.SnippetPage;
import cloud.codestore.client.usecases.readsnippet.ReadSnippetUseCase;
import cloud.codestore.jsonapi.document.SingleResourceDocument;
import cloud.codestore.jsonapi.link.Link;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A repository which saves/loads code snippets from the local {CodeStore} Core.
 */
@Repository
class LocalSnippetRepository implements ReadSnippetsUseCase, ReadSnippetUseCase, DeleteSnippetUseCase {

    private final HttpClient client;
    private final LocalTagRepository tagRepository;

    LocalSnippetRepository(HttpClient client, LocalTagRepository tagRepository) {
        this.client = client;
        this.tagRepository = tagRepository;
    }

    @Nonnull
    @Override
    public SnippetPage getFirstPage(
            @Nonnull String searchQuery,
            @Nonnull FilterProperties filterProperties
    ) {
        String url = client.getSnippetCollectionUrl();
        var uriBuilder = UriComponentsBuilder.fromUriString(url);

        if (StringUtils.hasText(searchQuery)) {
            uriBuilder.queryParam("searchQuery", searchQuery);
        }

        filterProperties.getTags().ifPresent(tags -> {
            String tagsCsv = String.join(",", tags);
            uriBuilder.queryParam("filter[tags]", tagsCsv);
        });

        filterProperties.getLanguage().ifPresent(language -> uriBuilder.queryParam("filter[language]", language.id()));

        url = uriBuilder.build().encode().toUri().toString();
        return getPage(url);
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
        SingleResourceDocument<SnippetResource> document = client.get(snippetUri, SnippetResource.class);
        SnippetResource snippetResource = document.getData();
        String tagsUri = snippetResource.getTags().getRelatedResourceLink();
        Set<Permission> permissions = getPermissions((ResourceMetaInfo) document.getMeta());

        return Snippet.builder()
                      .uri(snippetResource.getSelfLink())
                      .title(snippetResource.getTitle())
                      .description(snippetResource.getDescription())
                      .code(snippetResource.getCode())
                      .tags(tagRepository.get(tagsUri))
                      .permissions(permissions)
                      .build();
    }

    private Set<Permission> getPermissions(@Nullable ResourceMetaInfo meta) {
        if (meta == null || meta.getOperations() == null) {
            return Collections.emptySet();
        }

        return meta.getOperations()
                   .stream()
                   .map(operation -> {
                       if ("deleteSnippet".equals(operation.name())) {
                           return Permission.DELETE;
                       }
                       return null;
                   })
                   .filter(Objects::nonNull)
                   .collect(Collectors.toSet());
    }

    @Override
    public void deleteSnippet(@Nonnull String snippetUri) {
        client.delete(snippetUri);
    }
}
