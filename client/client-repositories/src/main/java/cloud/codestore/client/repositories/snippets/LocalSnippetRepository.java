package cloud.codestore.client.repositories.snippets;

import cloud.codestore.client.Language;
import cloud.codestore.client.Permission;
import cloud.codestore.client.Snippet;
import cloud.codestore.client.repositories.HttpClient;
import cloud.codestore.client.repositories.Repository;
import cloud.codestore.client.repositories.ResourceMetaInfo;
import cloud.codestore.client.repositories.language.LanguageResource;
import cloud.codestore.client.repositories.tags.LocalTagRepository;
import cloud.codestore.client.repositories.tags.TagResource;
import cloud.codestore.client.usecases.createsnippet.CreateSnippetUseCase;
import cloud.codestore.client.usecases.createsnippet.NewSnippetDto;
import cloud.codestore.client.usecases.deletesnippet.DeleteSnippetUseCase;
import cloud.codestore.client.usecases.listsnippets.*;
import cloud.codestore.client.usecases.readsnippet.ReadSnippetUseCase;
import cloud.codestore.jsonapi.document.SingleResourceDocument;
import cloud.codestore.jsonapi.link.Link;
import cloud.codestore.jsonapi.meta.MetaInformation;
import cloud.codestore.jsonapi.relationship.ToManyRelationship;
import cloud.codestore.jsonapi.relationship.ToOneRelationship;
import cloud.codestore.jsonapi.resource.ResourceIdentifierObject;
import cloud.codestore.jsonapi.resource.ResourceObject;
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
class LocalSnippetRepository implements ReadSnippetsUseCase, ReadSnippetUseCase, DeleteSnippetUseCase, CreateSnippetUseCase {

    private static final String FILTER_TAGS_PARAM = "filter[tags]";
    private static final String FILTER_LANGUAGE_PARAM = "filter[language]";
    private static final String SORT_PARAM = "sort";

    private final HttpClient client;
    private final LocalTagRepository tagRepository;

    LocalSnippetRepository(HttpClient client, LocalTagRepository tagRepository) {
        this.client = client;
        this.tagRepository = tagRepository;
    }

    @Nonnull
    @Override
    public SnippetPage getPage(
            @Nonnull String searchQuery,
            @Nonnull FilterProperties filterProperties,
            @Nonnull SortProperties sortProperties
    ) {
        String url = client.getSnippetCollectionUrl();
        var uriBuilder = UriComponentsBuilder.fromUriString(url);

        if (StringUtils.hasText(searchQuery)) {
            uriBuilder.queryParam("searchQuery", searchQuery);
        }

        filterProperties.getTags().ifPresent(tags -> {
            String tagsCsv = String.join(",", tags);
            uriBuilder.queryParam(FILTER_TAGS_PARAM, tagsCsv);
        });

        filterProperties.getLanguage().ifPresent(language -> uriBuilder.queryParam(FILTER_LANGUAGE_PARAM, language.id()));

        String sortValue = sortProperties.desc() ? "-" : "";
        sortValue += sortProperties.property().name().toLowerCase();
        uriBuilder.queryParam(SORT_PARAM, sortValue);

        url = uriBuilder.build().encode().toUri().toString();
        return getPage(url);
    }

    @Nonnull
    @Override
    public SnippetPage getPage(@Nonnull String pageUrl) {
        var document = client.getCollection(pageUrl, SnippetResource.class);
        Set<Permission> permissions = getPermissions((ResourceMetaInfo) document.getMeta());
        var items = Arrays.stream(document.getData(SnippetResource.class))
                          .map(resource -> new SnippetListItem(resource.getSelfLink(), resource.getTitle()))
                          .toList();

        String nextPageLink = document.getLinks().getHref(Link.NEXT);
        String nextPageUrl = Optional.ofNullable(nextPageLink).orElse("");
        return new SnippetPage(items, nextPageUrl, permissions);
    }

    @Nonnull
    @Override
    public Snippet readSnippet(String snippetUri) {
        SingleResourceDocument<SnippetResource> document = client.get(snippetUri, SnippetResource.class);
        return convertToSnippet(document.getData(), document.getMeta());
    }

    @Override
    public void deleteSnippet(@Nonnull String snippetUri) {
        client.delete(snippetUri);
    }

    @Override
    public Snippet create(@Nonnull NewSnippetDto snippetDto) {
        List<String> tags = Objects.requireNonNullElseGet(snippetDto.tags(), Collections::emptyList);
        List<TagResource> tagResources = createTags(tags);

        SnippetResource resource = new SnippetResource(
                snippetDto.title(),
                snippetDto.description(),
                snippetDto.code(),
                convert(snippetDto.language()),
                convert(tagResources)
        );

        var document = client.post(client.getSnippetCollectionUrl(), resource);
        return convertToSnippet(document.getData(), document.getMeta());
    }

    @Nonnull
    private List<TagResource> createTags(List<String> tags) {
        String url = client.getTagsCollectionUrl();
        return tags.stream()
                   .map(TagResource::new)
                   .map(resource -> client.post(url, resource))
                   .map(SingleResourceDocument::getData)
                   .toList();
    }

    private Snippet convertToSnippet(SnippetResource snippetResource, MetaInformation meta) {
        String tagsUri = snippetResource.getTags().getRelatedResourceLink();
        Set<Permission> permissions = getPermissions((ResourceMetaInfo) meta);
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
                   .map(operation -> switch (operation.name()) {
                       case "deleteSnippet" -> Permission.DELETE;
                       case "createSnippet" -> Permission.CREATE;
                       default -> null;
                   })
                   .filter(Objects::nonNull)
                   .collect(Collectors.toSet());
    }

    private ToOneRelationship<LanguageResource> convert(@Nullable Language language) {
        if (language == null) {
            return null;
        }

        var identifier = new ResourceIdentifierObject(LanguageResource.RESOURCE_TYPE, language.id());
        return new ToOneRelationship<LanguageResource>().setData(identifier);
    }

    private ToManyRelationship<TagResource> convert(@Nonnull List<TagResource> tagResources) {
        if (tagResources.isEmpty()) {
            return null;
        }

        var identifiers = tagResources.stream()
                                      .map(ResourceObject::getIdentifier)
                                      .toArray(ResourceIdentifierObject[]::new);

        return new ToManyRelationship<TagResource>().setData(identifiers);
    }
}
